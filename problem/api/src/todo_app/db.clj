(ns todo-app.db
  (:require
   [clojure.walk :as walk]
   [todo-app.config :refer [config]]
   [hikari-cp.core :refer [close-datasource make-datasource]]
   [honey.sql]
   [jsonista.core :as json]
   [mount.core :refer [defstate start]]
   [next.jdbc]
   [next.jdbc.date-time]
   [next.jdbc.optional :refer [as-lower-maps]]
   [next.jdbc.prepare :refer [SettableParameter]]
   [next.jdbc.result-set :refer [ReadableColumn]]
   [taoensso.telemere :as telemere])
  (:import
   [clojure.lang IPersistentMap IPersistentVector]
   [java.sql PreparedStatement]
   [org.postgresql.util PGobject]))

(defstate database-config
  :start
  {:adapter           "postgresql"
   :maximum-pool-size (get-in config [:database :pool-size] 5)
   :server-name       (get-in config [:database :host])
   :database-name     (get-in config [:database :database])
   :username          (get-in config [:database :user])
   :password          (get-in config [:database :pass])})

(defstate datasource
  :start
  (make-datasource database-config)
  :stop
  (close-datasource datasource))

(defn start-state!
  []
  (let [started
        (start #'todo-app.config/config
               #'todo-app.db/database-config
               #'todo-app.db/datasource)]
    (telemere/log! :debug started)))

(next.jdbc.date-time/read-as-instant)

;; json/jsonbの変換
(def mapper (json/object-mapper {:decode-key-fn keyword}))
(def ->json json/write-value-as-string)
(def <-json #(json/read-value % mapper))

(defn ->pgobject
  [x]
  (let [pgtype (or (:pgtype (meta x)) "jsonb")]
    (doto (PGobject.)
      (.setType pgtype)
      (.setValue (->json x)))))

(defn <-pgobject
  [^PGobject v]
  (let [type  (.getType v)
        value (.getValue v)]
    (if (#{"jsonb" "json"} type)
      (some-> value <-json (with-meta {:pgtype type}))
      value)))

(extend-protocol SettableParameter
  IPersistentMap
  (set-parameter [m ^PreparedStatement s i]
    (.setObject s i (->pgobject m)))

  IPersistentVector
  (set-parameter [v ^PreparedStatement s i]
    (.setObject s i (->pgobject v))))

(extend-protocol ReadableColumn
  java.sql.Array
  (read-column-by-label [^java.sql.Array v _]
    (vec (.getArray v)))
  (read-column-by-index [^java.sql.Array v _2 _3]
    (vec (.getArray v)))

  PGobject
  (read-column-by-label [^PGobject v _]
    (<-pgobject v))
  (read-column-by-index [^PGobject v _2 _3]
    (<-pgobject v)))

(defn format-query
  [body]
  (let [param-map (atom {})
        body (walk/prewalk
              (fn walker [item]
                (cond
                  (symbol? item)
                  (let [arg (name item)]
                    (swap! param-map assoc (keyword arg) item)
                    (keyword (str "?" arg)))

                  (list? item)
                  (let [arg (name (gensym (name (first item))))]
                    (swap! param-map assoc (keyword arg) item)
                    (keyword (str "?" arg)))

                  :else item))
              body)]
    (honey.sql/format body {:params @param-map})))

(defmacro defquery
  "HoneySQLクエリを関数として定義するマクロ。"
  [query-name & decl]
  (let [;; parse declaration
        parts (partition-by vector? (conj decl :placeholder))
        [[_ & metadata] [args] [body opts]] parts
        opts (merge {:builder-fn as-lower-maps} opts)
        sql-vec (when-not (::dynamic (meta query-name))
                  (format-query body))
        ds (gensym "datasource")
        extended-args (with-meta
                        (into [ds] args)
                        (meta args))
        attr-map (merge {:arglists `'(~args ~extended-args)}
                        (last (filter map? metadata)))
        metadata (if (map? (last metadata))
                   (conj (vec (butlast metadata)) attr-map)
                   (conj (vec metadata) attr-map))]
    `(defn ~(if sql-vec
              (vary-meta query-name assoc ::sql (first sql-vec))
              query-name)
       ~@metadata
       (~args
        (~query-name datasource ~@args))
       (~extended-args
        (let [sql# ~(or sql-vec `(format-query ~body))
              transform# ~(:transform opts identity)]
          (telemere/trace!
           {:id ::query
            :level :debug
            :data {::query ~(name query-name)
                   ::sql sql#}}
           (transform# (next.jdbc/execute! ~ds sql# ~opts))))))))

(def ^:dynamic *tx-active?* false)

(defmacro with-transaction
  [[bind datasource opts] & body]
  `(if *tx-active?*
     (let [savepoint# (.setSavepoint ~datasource)]
       (try
         (let [~bind ~datasource
               result# (do ~@body)]
           (.releaseSavepoint ~datasource savepoint#)
           result#)
         (catch Throwable t#
           (.rollback ~datasource savepoint#)
           (throw t#))))
     (binding [*tx-active?* true]
       (next.jdbc/with-transaction [~bind ~datasource ~opts]
         ~@body))))

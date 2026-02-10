(ns user
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.tools.namespace.repl :refer [refresh set-refresh-dirs]]
   [todo-app.core]
   [todo-app.db :as db]
   [todo-app.db.schema :as schema]
   [mount.core :refer [start stop]]
   [next.jdbc :as jdbc]
   [taoensso.telemere :as telemere])
  (:import
   [java.io File]
   [java.time ZoneId ZonedDateTime]
   [java.time.format DateTimeFormatter]))

(set-refresh-dirs "api/env/dev"
                  "api/src"
                  "shared/src")

(defonce _
  (do
    (telemere/set-min-level! :debug)
    (telemere/set-min-level! :slf4j :warn)
    (schema/ensure-db)
    (start)
    (schema/migrate)))

(defn reload
  []
  (stop)
  (refresh)
  (start))

(defn now
  []
  (let [utc (ZoneId/of "UTC")
        formatter (DateTimeFormatter/ofPattern "uuuuMMddHHmmss")
        timestamp (ZonedDateTime/now utc)]
    (.format formatter timestamp)))

(defn create-migration
  [migration-name]
  (let [base (str (now) "-" migration-name)
        folder "api/resources/migrations/"
        up-name (str folder base ".up.sql")
        down-name (str folder base ".down.sql")]
    (spit up-name (str "-- " up-name "\n"))
    (spit down-name (str "-- " down-name "\n"))))

(defn migrate
  []
  (db/start-state!)
  (schema/migrate))

(defn rollback
  [& args]
  (db/start-state!)
  (apply schema/rollback args))

(defmacro sql-of
  [query]
  `(-> ~query var meta :todo-app.db/sql))

(defn run-sql!
  [text]
  (jdbc/execute! todo-app.db/datasource [text]))

(def truncate-tables
  [:todo_items
   :todo_lists])

(def serial-key-tables
  [:todo_lists
   :todo_items])

(defn truncate-sql
  [tables]
  [(str "TRUNCATE TABLE "
        (str/join ", " (map name tables))
        " RESTART IDENTITY CASCADE;")])

(defn- run-seed!
  [seed-folder-name]
  (let [sql (truncate-sql truncate-tables)]
    (jdbc/execute! db/datasource sql))
  (let [seed-folder (io/file (io/resource seed-folder-name))
        seeds (sort-by #(File/.getName %) (.listFiles seed-folder))]
    (doseq [seed seeds
            query (str/split (slurp seed) #"(?<=;)")]
      (when-some [sql (some-> query str/trim-newline not-empty vector)]
        (jdbc/execute! db/datasource sql))))
  (doseq [table serial-key-tables]
    (let [sql [(str "select max(id) as max_id from " (name table))]
          [{max-id :max_id}] (jdbc/execute! db/datasource sql)
          seq-name (str (name table) "_id_seq")
          sql ["select setval(?, ?)" seq-name max-id]]
      (jdbc/execute! db/datasource sql))))

(defn seed!
  ([]
   (println "`seed!` はテーブルをまっさらにしてからデータを入れ直す。")
   (println "本当にそれでいいなら `(seed! \"i'm sure\")` で実行してください。")
   (println "対象テーブル：" truncate-tables))
  ([confirmation]
   (when (= confirmation "i'm sure")
     (run-seed! "seed"))))

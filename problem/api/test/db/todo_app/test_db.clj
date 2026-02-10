(ns todo-app.test-db
  (:require
   [todo-app.config :refer [config]]
   [todo-app.db :as db]
   [todo-app.db.schema :as schema]
   [hikari-cp.core :refer [close-datasource make-datasource]]
   [mount.core :as mount]
   [next.jdbc :as jdbc]
   [ragtime.next-jdbc :as rn]
   [ragtime.repl :as repl]))

(defonce ^:private test-datasource-atom
  (atom nil))

(defonce ^:private schema-version-atom
  (atom nil))

(defonce ^:private original-datasource-atom
  (atom nil))

(defn make-test-db-config
  []
  {:adapter "postgresql"
   :maximum-pool-size 1
   :server-name (get-in config [:database :host])
   :database-name (get-in config [:database :test-database])
   :username (get-in config [:database :user])
   :password (get-in config [:database :pass])})

(defn- compute-migrations-hash
  []
  (let [migrations (rn/load-resources "migrations")]
    (hash (mapv #(select-keys % [:id :up :down]) migrations))))

(defn- schema-up-to-date?
  [datasource]
  (when-let [cached-hash @schema-version-atom]
    (try
      (let [result (jdbc/execute-one! datasource
                                      ["SELECT COUNT(*) as cnt FROM ragtime_migrations"])]
        (and (pos? (:cnt result))
             (= cached-hash (compute-migrations-hash))))
      (catch Exception _
        false))))

(defn- migrate-test-database
  [datasource]
  (jdbc/with-transaction [tx datasource]
    (let [config {:datastore (rn/sql-database tx)
                  :migrations (rn/load-resources "migrations")}]
      (repl/migrate config)))
  (reset! schema-version-atom (compute-migrations-hash)))

(defn get-datasource
  []
  @test-datasource-atom)

(defn init!
  []
  (when (nil? @test-datasource-atom)
    (println "\n[test-db] Initializing test database...")
    (try
      (mount/start #'todo-app.config/config)

      (let [db-config (make-test-db-config)]
        (schema/ensure-db db-config)

        (let [datasource (make-datasource db-config)]
          (reset! test-datasource-atom datasource)

          (if (schema-up-to-date? datasource)
            (println "[test-db] Schema up-to-date, skipping migrations.")
            (do
              (jdbc/execute! datasource ["DROP SCHEMA IF EXISTS public CASCADE"])
              (jdbc/execute! datasource ["CREATE SCHEMA public"])
              (migrate-test-database datasource)))

          (reset! original-datasource-atom db/datasource)
          (alter-var-root #'db/datasource (constantly datasource))

          (println "[test-db] Test database ready.")))
      (catch Exception e
        (println "[test-db] Failed to initialize test database:" (.getMessage e))
        (when-let [ds @test-datasource-atom]
          (try (close-datasource ds) (catch Exception _)))
        (reset! test-datasource-atom nil)
        (reset! original-datasource-atom nil)
        (throw e)))))

(defn cleanup!
  []
  (when @test-datasource-atom
    (println "\n[test-db] Cleaning up test database...")
    (alter-var-root #'db/datasource (constantly @original-datasource-atom))
    (reset! original-datasource-atom nil)
    (when-let [ds @test-datasource-atom]
      (close-datasource ds))
    (reset! test-datasource-atom nil)
    (println "[test-db] Cleanup complete.")))

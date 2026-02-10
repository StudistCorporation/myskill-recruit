(ns todo-app.db.schema
  (:require
   [todo-app.db :as db]
   [hikari-cp.core :refer [close-datasource make-datasource]]
   [mount.core :refer [start]]
   [next.jdbc]
   [ragtime.next-jdbc :as rn]
   [ragtime.repl :as repl]
   [taoensso.telemere :refer [log!]])
  (:import [org.postgresql.util PSQLException]))

(defn config
  [datasource]
  {:datastore (rn/sql-database datasource)
   :migrations (rn/load-resources "migrations")})

(defn migrate
  []
  (log! :info "Migrating database")
  (repl/migrate (config db/datasource)))

(defn rollback
  ([]
   (rollback 1))
  ([amount-or-id]
   (log! :info "Rolling back database")
   (repl/rollback (config db/datasource) amount-or-id)))

(defn ensure-db
  ([]
   (let [started (start #'todo-app.config/config
                        #'todo-app.db/database-config)]
     (log! :debug started))
   (ensure-db db/database-config))

  ([database-config]
   (let [database (:database-name database-config)
         create-db-config (assoc database-config :database-name "postgres")
         datasource (make-datasource create-db-config)]
     (try
       (log! :info (str "Creating database " database))
       (next.jdbc/execute!
        datasource
        [(format "create database \"%s\"" database)])
       (catch PSQLException error
         (let [already-exists (format "ERROR: database \"%s\" already exists" database)]
           (if (= already-exists (.getMessage error))
             (log! :info (str "Database " database " already exists"))
             (throw error))))
       (finally
         (log! :debug "Closing temporary datasource")
         (close-datasource datasource))))))

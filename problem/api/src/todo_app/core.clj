(ns todo-app.core
  (:require [aleph.http :as http]
            [todo-app.db :as db]
            [todo-app.db.schema :as schema]
            [todo-app.router :as router]
            [mount.core :refer [defstate start stop]]
            [taoensso.telemere :as telemere])
  (:import [java.io Closeable]
           [java.util.concurrent Executors])
  (:gen-class))

(defstate http-server
  :start
  (let [server (http/start-server
                router/handler
                {:executor (Executors/newVirtualThreadPerTaskExecutor)
                 :port 3000
                 :idle-timeout 10000
                 :shutdown-timeout 5})]
    (telemere/log! :info "TODO App now listening on port 3000")
    server)
  :stop
  (.close ^Closeable http-server))

(defn shutdown
  []
  (stop)
  (telemere/stop-handlers!)
  (shutdown-agents))

(defn -main
  ([] (-main "server"))
  ([op & args]
   (.addShutdownHook
    (Runtime/getRuntime)
    (Thread. ^Runnable shutdown))

   (case op
     "migrate"
     (do
       (schema/ensure-db)
       (db/start-state!)
       (schema/migrate))

     "rollback"
     (do
       (schema/ensure-db)
       (db/start-state!)
       (schema/rollback (or (not-empty (first args)) 1)))

     "server"
     (start))))

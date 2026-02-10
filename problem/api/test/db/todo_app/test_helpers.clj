(ns todo-app.test-helpers
  (:require
   [clojure.test :refer [testing]]
   [todo-app.db :as db :refer [with-transaction]]
   [todo-app.test-db :as test-db]))

(defn with-test-database
  [f]
  (test-db/init!)
  (f))

(defn with-rollback
  [f]
  (with-transaction [tx (test-db/get-datasource) {:rollback-only true}]
    (with-redefs [db/datasource tx]
      (f))))

(defmacro testing-with-rollback
  [name & body]
  `(testing ~name
     (let [conn# db/datasource
           savepoint# (.setSavepoint conn#)]
       (try
         ~@body
         (finally
           (.rollback conn# savepoint#))))))

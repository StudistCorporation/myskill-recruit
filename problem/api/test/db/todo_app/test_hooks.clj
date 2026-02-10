(ns todo-app.test-hooks
  (:require
   [todo-app.test-db :as test-db]))

(defn prepare-db
  [testable _test-plan]
  (when (= :kaocha.type/clojure.test (:kaocha.testable/type testable))
    (test-db/init!))
  testable)

(defn cleanup-db
  [testable _test-plan]
  (when (= :kaocha.type/clojure.test (:kaocha.testable/type testable))
    (test-db/cleanup!))
  testable)

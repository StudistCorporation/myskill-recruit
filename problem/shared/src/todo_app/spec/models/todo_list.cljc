(ns todo-app.spec.models.todo-list
  (:refer-clojure :exclude [name]))

(def id
  [:and :int [:> 0]])

(def name
  [:string {:min 1 :max 255}])

(def display-order
  [:and :int [:>= 0]])

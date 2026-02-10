(ns todo-app.spec.models.todo-item)

(def id
  [:and :int [:> 0]])

(def todo-list-id
  [:and :int [:> 0]])

(def content
  [:string {:max 10000}])

(def done
  :boolean)

(def display-order
  [:and :int [:>= 0]])

(ns todo-app.spec.requests.todo-items
  (:require
   [todo-app.spec.models.todo-item :as todo-item]))

(def create-todo-item
  [:map
   [:content {:optional true} todo-item/content]])

(def update-todo-item
  [:map
   [:content {:optional true} todo-item/content]
   [:done {:optional true} todo-item/done]])

(def reorder-todo-items
  [:map
   [:ordered-ids [:vector todo-item/id]]])

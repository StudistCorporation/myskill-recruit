(ns todo-app.spec.requests.todo-lists
  (:require
   [todo-app.spec.models.todo-list :as todo-list]))

(def create-todo-list
  [:map
   [:name todo-list/name]])

(def update-todo-list
  [:map
   [:name todo-list/name]])

(def reorder-todo-lists
  [:map
   [:ordered-ids [:vector todo-list/id]]])

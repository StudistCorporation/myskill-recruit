(ns todo-app.routes.api.todo-lists
  (:require
   [todo-app.spec.models.todo-list :as todo-list]
   [todo-app.spec.requests.todo-lists :as requests]
   #?(:clj [todo-app.handlers.todo-lists :as handlers])))

(def routes
  ["/api/v1/todo-lists"
   [""
    {:name ::todo-lists
     :get {:handler #?(:clj handlers/list-todo-lists
                       :default true)}
     :post {:handler #?(:clj handlers/create-todo-list
                        :default true)
            :parameters {:body #'requests/create-todo-list}}}]
   ["/reorder"
    {:name ::reorder
     :put {:handler #?(:clj handlers/reorder-todo-lists
                       :default true)
           :parameters {:body #'requests/reorder-todo-lists}}}]
   ["/:list-id"
    {:name ::todo-list
     :parameters {:path [:map
                         [:list-id todo-list/id]]}
     :put {:handler #?(:clj handlers/update-todo-list
                       :default true)
           :parameters {:body #'requests/update-todo-list}}
     :delete {:handler #?(:clj handlers/delete-todo-list
                          :default true)}}]])

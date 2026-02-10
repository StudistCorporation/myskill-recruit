(ns todo-app.routes.api.todo-items
  (:require
   [todo-app.spec.models.todo-list :as todo-list]
   [todo-app.spec.models.todo-item :as todo-item]
   [todo-app.spec.requests.todo-items :as requests]
   #?(:clj [todo-app.handlers.todo-items :as handlers])))

(def routes
  [""
   ["/api/v1/todo-lists/:list-id/items"
    {:name ::items
     :parameters {:path [:map
                         [:list-id todo-list/id]]}
     :get {:handler #?(:clj handlers/list-items
                       :default true)}
     :post {:handler #?(:clj handlers/create-item
                        :default true)
            :parameters {:body #'requests/create-todo-item}}}]
   ["/api/v1/todo-items/:item-id"
    {:name ::item
     :parameters {:path [:map
                         [:item-id todo-item/id]]}
     :put {:handler #?(:clj handlers/update-item
                       :default true)
           :parameters {:body #'requests/update-todo-item}}
     :delete {:handler #?(:clj handlers/delete-item
                          :default true)}}]
   ["/api/v1/todo-items/reorder"
    {:name ::reorder
     :put {:handler #?(:clj handlers/reorder-items
                       :default true)
           :parameters {:body #'requests/reorder-todo-items}}}]])

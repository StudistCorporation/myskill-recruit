(ns todo-app.routes.web
  (:require
   [todo-app.spec.models.todo-list :as todo-list]
   #?(:cljs [re-frame.core :as rf])
   #?(:cljs [todo-app.views.pages.todo-list :as todo-list-page])
   #?(:cljs [todo-app.views.pages.empty :as empty-page])))

(def routes
  [""
   ["/"
    {:name ::home
     :controllers
     #?(:cljs [{:start (fn [_] (rf/dispatch [:todo-app.state.todo-lists/init]))}]
        :default [])
     :view #?(:cljs #'empty-page/view
              :default true)}]
   ["/lists/:list-id"
    {:name ::todo-list
     :parameters {:path {:list-id #'todo-list/id}}
     :controllers
     #?(:cljs [{:parameters {:path [:list-id]}
                :start (fn [{{list-id :list-id} :path}]
                         (rf/dispatch [:todo-app.state.todo-lists/init])
                         (rf/dispatch [:todo-app.state.todo-items/init list-id]))
                :stop (fn [_]
                        (rf/dispatch [:todo-app.state.todo-items/clear]))}]
        :default [])
     :view #?(:cljs #'todo-list-page/view
              :default true)}]])

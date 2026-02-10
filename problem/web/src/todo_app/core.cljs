(ns todo-app.core
  (:require ["react-dom/client" :as react.dom]
            [todo-app.state.http.effects]
            [todo-app.state.http.events]
            [todo-app.state.router.events]
            [todo-app.state.router.subs]
            [todo-app.state.todo-lists]
            [todo-app.state.todo-items]
            [todo-app.views :as views]
            [io.factorhouse.hsx.core :as hsx]
            [re-frame.core :as rf]))

(defonce root
  (react.dom/createRoot (js/document.getElementById "app")))

(defn ^:dev/after-load remount
  []
  (rf/clear-subscription-cache!)
  (hsx/memo-clear!)
  (.render root (hsx/create-element [views/main])))

(defn ^:export init
  []
  (rf/dispatch [:todo-app.state.router.events/start-router])
  (remount))

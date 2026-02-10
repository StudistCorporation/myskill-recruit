(ns todo-app.router
  (:require [re-frame.core :as rf]
            [reitit.coercion.malli]
            [reitit.frontend]
            [reitit.frontend.easy :as easy]
            [todo-app.routes.web :as web]
            [todo-app.state.router.events :as-alias events]))

(def router
  (reitit.frontend/router
   web/routes
   {:data {:coercion reitit.coercion.malli/coercion}}))

(defn on-navigate
  [new-match]
  (if new-match
    (rf/dispatch [::events/navigated new-match])
    (rf/dispatch [::events/route-not-found])))

(defn start-router
  []
  (easy/start!
   router
   on-navigate
   {:use-fragment false}))

(defn href
  ([route]
   (href route nil nil))
  ([route params]
   (href route params nil))
  ([route params query]
   (easy/href route params query)))

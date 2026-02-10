(ns todo-app.router
  (:require
   [todo-app.routes.api.todo-lists :as todo-lists-routes]
   [todo-app.routes.api.todo-items :as todo-items-routes]
   [todo-app.wrappers.cors :refer [wrap-cors]]
   [todo-app.wrappers.exception :refer [wrap-exception]]
   [muuntaja.middleware :refer [wrap-format]]
   [reitit.coercion.malli :as malli]
   [reitit.ring]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.parameters :refer [parameters-middleware]]))

(def api-router
  (reitit.ring/router
   [todo-lists-routes/routes
    todo-items-routes/routes]
   {:conflicts nil
    :data {:coercion malli/coercion
           :middleware [wrap-exception
                        wrap-format
                        wrap-cors
                        parameters-middleware
                        coercion/coerce-exceptions-middleware
                        coercion/coerce-request-middleware]}
    :inject-router? false
    :inject-match? false}))

(def handler
  (reitit.ring/ring-handler
   api-router
   (reitit.ring/routes
    (reitit.ring/create-default-handler))
   {:inject-router? false
    :inject-match? false}))

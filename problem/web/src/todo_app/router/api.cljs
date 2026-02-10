(ns todo-app.router.api
  (:require [todo-app.routes.api.todo-lists]
            [todo-app.routes.api.todo-items]
            [reitit.coercion.malli]
            [reitit.ring]))

(def router
  (reitit.ring/router
   [todo-app.routes.api.todo-lists/routes
    todo-app.routes.api.todo-items/routes]
   {:conflicts nil
    :data {:coercion reitit.coercion.malli/coercion}}))

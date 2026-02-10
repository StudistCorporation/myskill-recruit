(ns todo-app.state.router.subs
  (:require
   [todo-app.state.router.events :as-alias events]
   [re-frame.core :as rf]))

(rf/reg-sub
 ::started?
 :-> ::events/started?)

(rf/reg-sub
 ::navigation-attempted?
 :-> ::events/navigation-attempted?)

(rf/reg-sub
 ::current-route
 :-> ::events/current-route)

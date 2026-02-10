(ns todo-app.state.router.events
  (:require [re-frame.core :as rf]
            [reitit.frontend.easy :as easy]
            [reitit.frontend.controllers :refer [apply-controllers]]
            [todo-app.router :as router]))

(rf/reg-fx
 ::start-router
 (fn [_]
   (router/start-router)))

(rf/reg-event-fx
 ::start-router
 (fn [{:keys [db]} _]
   {:db (assoc db ::started? true)
    :fx [[::start-router nil]]}))

(rf/reg-event-fx
 ::push-state
 (fn [_ [_ route params query]]
   {::push-state [route params query]}))

(rf/reg-fx
 ::push-state
 (fn [[route params query]]
   (easy/push-state route params query)))

(rf/reg-event-db
 ::navigated
 (fn [db [_ new-match]]
   (let [old-match (::current-route db)
         controllers (apply-controllers
                      (:controllers old-match)
                      new-match)]
     (assoc db
            ::current-route (assoc new-match :controllers controllers)
            ::navigation-attempted? true))))

(rf/reg-event-db
 ::route-not-found
 (fn [db _]
   (assoc db ::navigation-attempted? true)))

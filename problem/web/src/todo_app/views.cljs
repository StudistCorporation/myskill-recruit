(ns todo-app.views
  (:require
   [todo-app.state.router.subs :as-alias router]
   [todo-app.views.layout :as layout]
   [re-frame.core :as rf]
   [shadow.css :refer [css]]))

(def $main
  (css :text-black-500
       {:font "normal normal 14px/160% 'Noto Sans JP', sans-serif"}))

(def $loading
  (css :flex :justify-center :items-center :h-screen :bg-white-300))

(defn main
  [& _]
  (let [router-ready? @(rf/subscribe [::router/started?])
        navigation-attempted? @(rf/subscribe [::router/navigation-attempted?])
        match @(rf/subscribe [::router/current-route])]
    (if (or (not router-ready?)
            (not navigation-attempted?))
      [:div {:class [$loading]} "Loading..."]
      [:div {:class [$main]}
       (let [data (:data match)
             {:keys [view]
              :or {view (fn [_] [:div "Not Found"])}} data
             view (if (var? view) @view view)]
         [layout/view match [view match]])])))

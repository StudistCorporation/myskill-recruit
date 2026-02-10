(ns todo-app.views.layout
  (:require
   [todo-app.views.sidebar :as sidebar]
   [shadow.css :refer [css]]))

(def $wrap
  (css :grid :h-screen :bg-white-300
       {:grid-template-columns "240px 1fr"
        :min-width "800px"}))

(def $content-area
  (css :overflow-auto :bg-white-50))

(defn view
  [route child]
  [:div {:class [$wrap]}
   [sidebar/view route]
   [:div {:class [$content-area]}
    child]])

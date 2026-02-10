(ns todo-app.views.pages.empty
  (:require
   [shadow.css :refer [css]]))

(def $container
  (css :flex :items-center :justify-center :h-full :text-gray-600))

(def $message
  (css :text-center))

(def $icon
  (css :text-4xl :mb-4 :text-gray-400))

(def $text
  (css :text-lg))

(defn view
  [_match]
  [:div {:class [$container]}
   [:div {:class [$message]}
    [:div {:class [$icon]} "\u2611"]
    [:div {:class [$text]} "サイドバーからリストを選択してください"]]])

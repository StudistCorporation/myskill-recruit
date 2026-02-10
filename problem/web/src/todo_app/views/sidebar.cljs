(ns todo-app.views.sidebar
  (:require
   ["react" :as react]
   [todo-app.router :as router]
   [todo-app.routes.web :as-alias web]
   [todo-app.state.todo-lists :as todo-lists]
   [re-frame.core :as rf]
   [shadow.css :refer [css]]))

(def $sidebar
  (css :flex :flex-col :h-full :bg-black-600 :text-white-50
       {:width "240px"
        :min-width "240px"}))

(def $header
  (css :px-4 :py-4 :text-lg :font-bold
       {:border-bottom "1px solid rgba(255,255,255,0.1)"}))

(def $list-area
  (css :flex-1 :overflow-auto :py-2))

(def $list-item
  (css :flex :items-center :px-4 :py-2 :cursor-pointer
       {:transition "background-color 0.15s"}
       ["&:hover" {:background-color "rgba(255,255,255,0.1)"}]))

(def $list-item-active
  (css {:background-color "rgba(255,255,255,0.15)"}))

(def $list-name
  (css :flex-1 :truncate :text-sm))

(def $add-area
  (css :px-4 :py-3
       {:border-top "1px solid rgba(255,255,255,0.1)"}))

(def $add-btn
  (css :w-full :py-2 :px-3 :text-sm :text-center :cursor-pointer :rounded
       {:background "rgba(255,255,255,0.1)"
        :border "1px solid rgba(255,255,255,0.2)"
        :color "#fff"
        :transition "background-color 0.15s"}
       ["&:hover" {:background "rgba(255,255,255,0.2)"}]))

(defn- list-item-view
  [{:keys [id name]} current-list-id on-drag-start on-drop]
  (let [active? (= id current-list-id)]
    [:div {:key id
           :class [$list-item (when active? $list-item-active)]
           :draggable true
           :on-drag-start #(on-drag-start id %)
           :on-drag-over #(.preventDefault %)
           :on-drop #(on-drop id %)
           :on-click #(set! (.-href js/window.location)
                            (router/href ::web/todo-list {:list-id id}))}
     [:span {:class [$list-name]} name]]))

(defn view
  [_route]
  (let [lists @(rf/subscribe [::todo-lists/lists])
        current-route @(rf/subscribe [:todo-app.state.router.subs/current-route])
        current-list-id (some-> current-route :parameters :path :list-id)
        drag-source (react/useRef nil)]
    [:div {:class [$sidebar]}
     [:div {:class [$header]} "Todo Lists"]
     [:div {:class [$list-area]}
      (for [list lists]
        ^{:key (:id list)}
        [list-item-view list current-list-id
         (fn [id _e] (set! (.-current drag-source) id))
         (fn [target-id _e]
           (when-let [source-id (.-current drag-source)]
             (when (not= source-id target-id)
               (let [ids (mapv :id lists)
                     source-idx (.indexOf ids source-id)
                     target-idx (.indexOf ids target-id)
                     without-source (into (subvec ids 0 source-idx)
                                          (subvec ids (inc source-idx)))
                     reordered (into (subvec without-source 0 target-idx)
                                     (cons source-id (subvec without-source target-idx)))]
                 (rf/dispatch [::todo-lists/reorder-lists reordered])))))])]
     [:div {:class [$add-area]}
      [:button {:class [$add-btn]
                :on-click (fn [_]
                            (let [name (js/prompt "新しいリスト名:")]
                              (when (and name (not= name ""))
                                (rf/dispatch [::todo-lists/create-list name]))))}
       "+ 新規リスト"]]]))

(ns todo-app.state.todo-items
  (:require
   [todo-app.routes.api.todo-items :as-alias api]
   [todo-app.state.http.events :as-alias http]
   [re-frame.core :as rf]))

(rf/reg-event-fx
 ::init
 (fn [{:keys [db]} [_ list-id]]
   {:db (assoc db ::current-list-id list-id)
    :fx [[:dispatch [::fetch-items list-id]]]}))

(rf/reg-event-db
 ::clear
 (fn [db _]
   (dissoc db ::items ::current-list-id ::filter)))

(rf/reg-event-db
 ::set-filter
 (fn [db [_ filter-type]]
   (assoc db ::filter filter-type)))

(rf/reg-event-fx
 ::fetch-items
 (fn [_ [_ list-id]]
   {:fx [[:dispatch [::http/fetch ::api/items
                     {:opts {:method :get}
                      :params {:list-id list-id}
                      :on-success [::fetch-items-success]}]]]}))

(rf/reg-event-db
 ::fetch-items-success
 (fn [db [_ items]]
   (assoc db ::items items)))

(rf/reg-event-fx
 ::create-item
 (fn [{:keys [db]} [_ content]]
   (let [list-id (::current-list-id db)]
     {:fx [[:dispatch [::http/fetch ::api/items
                       {:opts {:method :post}
                        :params {:list-id list-id
                                 :content (or content "")}
                        :on-success [::create-item-success]}]]]})))

(rf/reg-event-db
 ::create-item-success
 (fn [db [_ new-item]]
   (update db ::items (fnil conj []) new-item)))

(rf/reg-event-fx
 ::update-item
 (fn [_ [_ id updates]]
   {:fx [[:dispatch [::http/fetch ::api/item
                     {:opts {:method :put}
                      :params (merge {:item-id id} updates)
                      :on-success [::update-item-success]}]]]}))

(rf/reg-event-db
 ::update-item-success
 (fn [db [_ updated-item]]
   (update db ::items
           (fn [items]
             (mapv #(if (= (:id %) (:id updated-item))
                      updated-item
                      %)
                   items)))))

(rf/reg-event-fx
 ::delete-item
 (fn [_ [_ id]]
   {:fx [[:dispatch [::http/fetch ::api/item
                     {:opts {:method :delete}
                      :params {:item-id id}
                      :on-success [::delete-item-success id]}]]]}))

(rf/reg-event-db
 ::delete-item-success
 (fn [db [_ id _response]]
   (update db ::items
           (fn [items]
             (into [] (remove #(= (:id %) id)) items)))))

(rf/reg-event-fx
 ::reorder-items
 (fn [{:keys [db]} [_ ordered-ids]]
   (let [items (::items db)
         id->item (into {} (map (juxt :id identity)) items)
         reordered (mapv #(assoc (id->item %) :display-order %2)
                         ordered-ids (range))]
     {:db (assoc db ::items reordered)
      :fx [[:dispatch [::http/fetch ::api/reorder
                       {:opts {:method :put}
                        :params {:ordered-ids ordered-ids}
                        :on-success [::noop]}]]]})))

(rf/reg-event-db ::noop (fn [db _] db))

(rf/reg-sub
 ::items
 (fn [db _]
   (get db ::items [])))

(rf/reg-sub
 ::current-list-id
 (fn [db _]
   (::current-list-id db)))

(rf/reg-sub
 ::current-filter
 (fn [db _]
   (get db ::filter :all)))

(rf/reg-sub
 ::filtered-items
 :<- [::items]
 :<- [::current-filter]
 (fn [[items filter-type] _]
   (case filter-type
     :active (filterv (complement :done) items)
     :done (filterv :done items)
     items)))

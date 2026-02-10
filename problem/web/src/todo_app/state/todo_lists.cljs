(ns todo-app.state.todo-lists
  (:require
   [todo-app.routes.api.todo-lists :as-alias api]
   [todo-app.state.http.events :as-alias http]
   [re-frame.core :as rf]))

(rf/reg-event-fx
 ::init
 (fn [{:keys [db]} _]
   (when-not (::lists db)
     {:fx [[:dispatch [::fetch-lists]]]})))

(rf/reg-event-fx
 ::fetch-lists
 (fn [_ _]
   {:fx [[:dispatch [::http/fetch ::api/todo-lists
                     {:opts {:method :get}
                      :on-success [::fetch-lists-success]}]]]}))

(rf/reg-event-db
 ::fetch-lists-success
 (fn [db [_ lists]]
   (assoc db ::lists lists)))

(rf/reg-event-fx
 ::create-list
 (fn [_ [_ name]]
   {:fx [[:dispatch [::http/fetch ::api/todo-lists
                     {:opts {:method :post}
                      :params {:name name}
                      :on-success [::create-list-success]}]]]}))

(rf/reg-event-db
 ::create-list-success
 (fn [db [_ new-list]]
   (update db ::lists (fnil conj []) new-list)))

(rf/reg-event-fx
 ::update-list
 (fn [_ [_ id name]]
   {:fx [[:dispatch [::http/fetch ::api/todo-list
                     {:opts {:method :put}
                      :params {:list-id id :name name}
                      :on-success [::update-list-success]}]]]}))

(rf/reg-event-db
 ::update-list-success
 (fn [db [_ updated-list]]
   (update db ::lists
           (fn [lists]
             (mapv #(if (= (:id %) (:id updated-list))
                      updated-list
                      %)
                   lists)))))

(rf/reg-event-fx
 ::delete-list
 (fn [_ [_ id]]
   {:fx [[:dispatch [::http/fetch ::api/todo-list
                     {:opts {:method :delete}
                      :params {:list-id id}
                      :on-success [::delete-list-success id]}]]]}))

(rf/reg-event-db
 ::delete-list-success
 (fn [db [_ id _response]]
   (update db ::lists
           (fn [lists]
             (into [] (remove #(= (:id %) id)) lists)))))

(rf/reg-event-fx
 ::reorder-lists
 (fn [{:keys [db]} [_ ordered-ids]]
   (let [lists (::lists db)
         id->list (into {} (map (juxt :id identity)) lists)
         reordered (mapv #(assoc (id->list %) :display-order %2)
                         ordered-ids (range))]
     {:db (assoc db ::lists reordered)
      :fx [[:dispatch [::http/fetch ::api/reorder
                       {:opts {:method :put}
                        :params {:ordered-ids ordered-ids}
                        :on-success [::fetch-lists]}]]]})))

(rf/reg-sub
 ::lists
 (fn [db _]
   (get db ::lists [])))

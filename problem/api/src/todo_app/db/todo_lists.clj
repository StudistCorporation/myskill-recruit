(ns todo-app.db.todo-lists
  (:require
   [todo-app.db :as db :refer [defquery]]))

(defn- format-list
  [row]
  (some-> row
          (select-keys [:todo_lists/id
                        :todo_lists/name
                        :todo_lists/display_order
                        :todo_lists/created_at
                        :todo_lists/updated_at])
          (clojure.set/rename-keys {:todo_lists/id :id
                                    :todo_lists/name :name
                                    :todo_lists/display_order :display-order
                                    :todo_lists/created_at :created-at
                                    :todo_lists/updated_at :updated-at})))

(defquery get-todo-lists
  []
  {:select [:*]
   :from [:todo_lists]
   :order-by [[:display_order :asc] [:id :asc]]}
  {:transform #(mapv format-list %)})

(defquery ^::db/dynamic insert-todo-list!
  [todo-list]
  (let [{:keys [name display_order]} todo-list]
    {:insert-into :todo_lists
     :values [{:name name
               :display_order (or display_order 0)}]
     :returning [:*]})
  {:transform #(some-> % first format-list)})

(defquery ^::db/dynamic update-todo-list!
  [todo-list]
  (let [{:keys [id name]} todo-list]
    {:update :todo_lists
     :set {:name name}
     :where [:= :id id]
     :returning [:*]})
  {:transform #(some-> % first format-list)})

(defquery ^::db/dynamic delete-todo-list!
  [todo-list]
  (let [{:keys [id]} todo-list]
    {:delete-from :todo_lists
     :where [:= :id id]
     :returning [:*]})
  {:transform #(some-> % first format-list)})

(defquery get-max-display-order
  []
  {:select [[[:coalesce [:max :display_order] -1] :max_order]]
   :from [:todo_lists]}
  {:transform #(-> % first :max_order)})

(defn reorder-todo-lists!
  [ordered-ids]
  (db/with-transaction [tx db/datasource {}]
    (doseq [[idx id] (map-indexed vector ordered-ids)]
      (next.jdbc/execute!
       tx
       (db/format-query
        {:update :todo_lists
         :set {:display_order idx}
         :where [:= :id id]})))))

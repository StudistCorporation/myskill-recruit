(ns todo-app.db.todo-items
  (:require
   [todo-app.db :as db :refer [defquery]]
  ))

(defn- format-item
  [row]
  (some-> row
          (select-keys [:todo_items/id
                        :todo_items/todo_list_id
                        :todo_items/content
                        :todo_items/done
                        :todo_items/display_order
                        :todo_items/created_at
                        :todo_items/updated_at])
          (clojure.set/rename-keys {:todo_items/id :id
                                    :todo_items/todo_list_id :todo-list-id
                                    :todo_items/content :content
                                    :todo_items/done :done
                                    :todo_items/display_order :display-order
                                    :todo_items/created_at :created-at
                                    :todo_items/updated_at :updated-at})))

(defquery get-items-by-list-id
  [list-id]
  {:select [:*]
   :from [:todo_items]
   :where [:= :todo_list_id list-id]
   :order-by [[:display_order :asc] [:id :asc]]}
  {:transform #(mapv format-item %)})

(defquery ^::db/dynamic insert-item!
  [item]
  (let [{:keys [todo_list_id content display_order]} item]
    {:insert-into :todo_items
     :values [{:todo_list_id todo_list_id
               :content (or content "")
               :display_order (or display_order 0)}]
     :returning [:*]})
  {:transform #(some-> % first format-item)})

(defquery ^::db/dynamic update-item!
  [item]
  (let [{:keys [id]} item
        set-map (cond-> {}
                  (contains? item :content) (assoc :content (:content item))
                  (contains? item :done) (assoc :done (:done item)))]
    {:update :todo_items
     :set set-map
     :where [:= :id id]
     :returning [:*]})
  {:transform #(some-> % first format-item)})

(defquery ^::db/dynamic delete-item!
  [item]
  (let [{:keys [id]} item]
    {:delete-from :todo_items
     :where [:= :id id]
     :returning [:*]})
  {:transform #(some-> % first format-item)})

(defquery get-max-display-order-for-list
  [list-id]
  {:select [[[:coalesce [:max :display_order] -1] :max_order]]
   :from [:todo_items]
   :where [:= :todo_list_id list-id]}
  {:transform #(-> % first :max_order)})

(defquery ^::db/dynamic get-item-by-id
  "
  item-idをもとにアイテムを取得するクエリ
   
  args:
   - item-id: 取得対象のアイテムのID
  
  return:
    - アイテムの内容(content)と完了状態(done)を含むマップ
      (ex: {:content \"タスクの内容\", :done false})
  "
  [item-id]
  {:select [:content :done]
   :from [:todo_items]
   :where [:= :id item-id]}
  {:transform #(-> % first)})

(defn reorder-items!
  [ordered-ids]
  (db/with-transaction [tx db/datasource {}]
    (doseq [[idx id] (map-indexed vector ordered-ids)]
      (next.jdbc/execute!
       tx
       (db/format-query
        {:update :todo_items
         :set {:display_order idx}
         :where [:= :id id]})))))

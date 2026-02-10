(ns todo-app.handlers.todo-items
  (:require
   [todo-app.db.todo-items :as item-db]
   [ring.util.http-response :refer [not-found ok]]))

(defn list-items
  [{{{list-id :list-id} :path} :parameters}]
  (ok (item-db/get-items-by-list-id list-id)))

(defn create-item
  [{{{list-id :list-id} :path
     {:keys [content]} :body} :parameters}]
  (let [max-order (item-db/get-max-display-order-for-list list-id)
        item (item-db/insert-item!
              {:todo_list_id list-id
               :content (or content "")
               :display_order (inc max-order)})]
    (ok item)))

(defn update-item
  [request]
  ;; TODO: アイテムの更新ロジックを実装してください
  ;; - リクエストから item-id と body (content, done) を取得
  ;; - item-db/update-item! を呼び出して更新
  ;; - 更新成功時は ok、見つからない場合は not-found を返す
  ;; ヒント: create-item や delete-item の実装パターンを参考にしてください
  (ok {}))

(defn delete-item
  [{{{item-id :item-id} :path} :parameters}]
  (if (item-db/delete-item! {:id item-id})
    (ok {})
    (not-found {:message "Item not found"})))

(defn reorder-items
  [{{{:keys [ordered-ids]} :body} :parameters}]
  (item-db/reorder-items! ordered-ids)
  (ok {}))

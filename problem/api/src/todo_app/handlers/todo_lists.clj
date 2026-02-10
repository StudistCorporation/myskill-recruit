(ns todo-app.handlers.todo-lists
  (:require
   [todo-app.db.todo-lists :as list-db]
   [ring.util.http-response :refer [not-found ok]]))

(defn list-todo-lists
  [_request]
  (ok (list-db/get-todo-lists)))

(defn create-todo-list
  [{{{:keys [name]} :body} :parameters}]
  (let [max-order (list-db/get-max-display-order)
        todo-list (list-db/insert-todo-list!
                   {:name name
                    :display_order (inc max-order)})]
    (ok todo-list)))

(defn update-todo-list
  [{{{list-id :list-id} :path
     {:keys [name]} :body} :parameters}]
  (if-let [todo-list (list-db/update-todo-list!
                      {:id list-id
                       :name name})]
    (ok todo-list)
    (not-found {:message "List not found"})))

(defn delete-todo-list
  [{{{list-id :list-id} :path} :parameters}]
  (if (list-db/delete-todo-list! {:id list-id})
    (ok {})
    (not-found {:message "List not found"})))

(defn reorder-todo-lists
  [{{{:keys [ordered-ids]} :body} :parameters}]
  (list-db/reorder-todo-lists! ordered-ids)
  (ok (list-db/get-todo-lists)))

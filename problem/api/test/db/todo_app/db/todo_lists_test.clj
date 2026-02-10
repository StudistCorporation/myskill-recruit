(ns todo-app.db.todo-lists-test
  (:require
   [clojure.test :refer [deftest is use-fixtures]]
   [todo-app.db.todo-lists :as todo-lists]
   [todo-app.test-helpers :refer [testing-with-rollback with-rollback with-test-database]]))

(use-fixtures :once with-test-database)
(use-fixtures :each with-rollback)

(deftest get-todo-lists-test
  (testing-with-rollback "リストが空の場合、空ベクタを返す"
                         (is (= [] (todo-lists/get-todo-lists)))))

(deftest insert-todo-list-test
  (testing-with-rollback "リストを作成できる"
                         (let [result (todo-lists/insert-todo-list! {:name "テストリスト"
                                                                     :display_order 0})]
                           (is (some? (:id result)))
                           (is (= "テストリスト" (:name result)))
                           (is (= 0 (:display-order result)))))

  (testing-with-rollback "複数リストを作成して取得できる"
                         (todo-lists/insert-todo-list! {:name "リスト1" :display_order 0})
                         (todo-lists/insert-todo-list! {:name "リスト2" :display_order 1})
                         (let [lists (todo-lists/get-todo-lists)]
                           (is (= 2 (count lists)))
                           (is (= ["リスト1" "リスト2"] (mapv :name lists))))))

(deftest update-todo-list-test
  (testing-with-rollback "リスト名を更新できる"
                         (let [created (todo-lists/insert-todo-list! {:name "元の名前" :display_order 0})
                               updated (todo-lists/update-todo-list! {:id (:id created) :name "新しい名前"})]
                           (is (= "新しい名前" (:name updated)))
                           (is (= (:id created) (:id updated))))))

(deftest delete-todo-list-test
  (testing-with-rollback "リストを削除できる"
                         (let [created (todo-lists/insert-todo-list! {:name "削除対象" :display_order 0})]
                           (todo-lists/delete-todo-list! {:id (:id created)})
                           (is (= [] (todo-lists/get-todo-lists))))))

(deftest get-max-display-order-test
  (testing-with-rollback "リストがない場合は-1を返す"
                         (is (= -1 (todo-lists/get-max-display-order))))

  (testing-with-rollback "最大display_orderを返す"
                         (todo-lists/insert-todo-list! {:name "リスト1" :display_order 5})
                         (todo-lists/insert-todo-list! {:name "リスト2" :display_order 10})
                         (is (= 10 (todo-lists/get-max-display-order)))))

(deftest reorder-todo-lists-test
  (testing-with-rollback "リストの並び替えができる"
                         (let [l1 (todo-lists/insert-todo-list! {:name "A" :display_order 0})
                               l2 (todo-lists/insert-todo-list! {:name "B" :display_order 1})
                               l3 (todo-lists/insert-todo-list! {:name "C" :display_order 2})]
                           (todo-lists/reorder-todo-lists! [(:id l3) (:id l1) (:id l2)])
                           (let [lists (todo-lists/get-todo-lists)]
                             (is (= ["C" "A" "B"] (mapv :name lists)))))))

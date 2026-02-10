(ns todo-app.spec-test
  (:require [todo-app.vitest :refer [deftest is testing]]
            [malli.core :as m]
            [todo-app.spec.models.todo-list :as todo-list]
            [todo-app.spec.models.todo-item :as todo-item]
            [todo-app.spec.requests.todo-lists :as req-lists]
            [todo-app.spec.requests.todo-items :as req-items]))

(deftest todo-list-id-spec-test
  (testing "todo-list/idのバリデーション"
    (is (true? (m/validate todo-list/id 1)))
    (is (true? (m/validate todo-list/id 100)))
    (is (false? (m/validate todo-list/id 0)))
    (is (false? (m/validate todo-list/id -1)))
    (is (false? (m/validate todo-list/id "abc")))))

(deftest todo-list-name-spec-test
  (testing "todo-list/nameのバリデーション"
    (is (true? (m/validate todo-list/name "買い物リスト")))
    (is (false? (m/validate todo-list/name "")))
    (is (false? (m/validate todo-list/name 123)))))

(deftest todo-item-content-spec-test
  (testing "todo-item/contentのバリデーション"
    (is (true? (m/validate todo-item/content "牛乳を買う")))
    (is (true? (m/validate todo-item/content "")))
    (is (false? (m/validate todo-item/content 123)))))

(deftest create-todo-list-request-test
  (testing "create-todo-listリクエストのバリデーション"
    (is (true? (m/validate req-lists/create-todo-list {:name "新しいリスト"})))
    (is (false? (m/validate req-lists/create-todo-list {})))
    (is (false? (m/validate req-lists/create-todo-list {:name ""})))))

(deftest create-todo-item-request-test
  (testing "create-todo-itemリクエストのバリデーション"
    (is (true? (m/validate req-items/create-todo-item {})))
    (is (true? (m/validate req-items/create-todo-item {:content "タスク"})))))

(deftest update-todo-item-request-test
  (testing "update-todo-itemリクエストのバリデーション"
    (is (true? (m/validate req-items/update-todo-item {:content "更新後"})))
    (is (true? (m/validate req-items/update-todo-item {:done true})))
    (is (true? (m/validate req-items/update-todo-item {:content "更新後" :done false})))))

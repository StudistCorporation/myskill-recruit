(ns todo-app.vitest
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def git-root
  (try
    (let [safe-cmd ^String/1 (into-array String ["git" "config" "--global"
                                                 "--add" "safe.directory"
                                                 (System/getProperty "user.dir")])
          safe-proc (.exec (Runtime/getRuntime) safe-cmd)]
      (.waitFor safe-proc))
    (let [command ^String/1 (into-array String ["git" "rev-parse" "--show-toplevel"])
          process (.exec (Runtime/getRuntime) command)
          exit-code (.waitFor process)]
      (if (zero? exit-code)
        (-> (.getInputStream process) slurp str/trim io/file .toPath)
        (-> (System/getProperty "user.dir") io/file .toPath)))
    (catch Exception _
      (-> (System/getProperty "user.dir") io/file .toPath))))

(defn full-path
  [filename]
  (let [file-path (-> filename io/resource io/file .toPath)]
    (str (.relativize git-root file-path))))

(defmacro deftest
  [sym & body]
  (let [title (str (or (namespace sym) (str *ns*)) "/" (name sym))]
    `(todo-app.vitest/suite ~title (fn ~sym [] ~@body))))

(defmacro testing
  [title & body]
  `(todo-app.vitest/describe ~title (fn ~(gensym "testing") [] ~@body)))

(defmulti assert-expr
  (fn dispatcher [[head & _tail]] head))

(defmethod assert-expr 'true?
  expect-true? [[_ actual :as form]]
  `(-> (todo-app.vitest/expect ~actual)
       (.todo_app_equality_matcher
        (fn [] {:form '~actual
                :expected true
                :message (str "Expected " '~actual " to be true.")
                :meta ~(update (meta form) :file full-path)}))))

(defmethod assert-expr 'false?
  expect-false? [[_ actual :as form]]
  `(-> (todo-app.vitest/expect ~actual)
       (.todo_app_equality_matcher
        (fn [] {:form '~actual
                :expected false
                :message (str "Expected " '~actual " to be false.")
                :meta ~(update (meta form) :file full-path)}))))

(defmethod assert-expr 'nil?
  expect-nil? [[_ actual :as form]]
  `(-> (todo-app.vitest/expect ~actual)
       (.todo_app_equality_matcher
        (fn [] {:form '~actual
                :expected nil
                :message (str "Expected " '~actual " to be nil.")
                :meta ~(update (meta form) :file full-path)}))))

(defmethod assert-expr '=
  expect-equal [[_ expected actual :as form]]
  `(-> (todo-app.vitest/expect ~actual)
       (.todo_app_equality_matcher
        (fn [] {:form '~form
                :expected ~expected
                :message (str "Expected " '~actual " to be equal to "
                              (todo-app.vitest/readable-value '~expected) ".")
                :meta ~(update (meta form) :file full-path)}))))

(defmethod assert-expr :default
  expect-default [form]
  `(-> (todo-app.vitest/expect
        (fn [] ~form))
       (.toBeTruthy)
       (.todo_app_value_matcher
        (fn [] {:form '~form
                :expected true
                :message (str "Expected " '~form " to be truthy.")
                :meta ~(update (meta form) :file full-path)}))))

(defmacro is
  ([form]
   `(todo-app.vitest/is ~form ~(pr-str form)))
  ([form message]
   (let [expectation (assert-expr form)]
     `(todo-app.vitest/test
       ~message
       (fn [] ~expectation)))))

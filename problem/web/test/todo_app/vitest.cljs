(ns todo-app.vitest
  (:refer-clojure :exclude [test])
  (:require ["vitest" :as vitest]
            [clojure.pprint :refer [pprint]])
  (:require-macros [todo-app.vitest]))

(defn suite
  [title f]
  (vitest/suite title f))

(defn describe
  [title f]
  (vitest/describe title f))

(defn test
  [title f]
  (vitest/test title f))

(defn expect
  [f]
  (vitest/expect f))

(defn readable-value
  [v]
  (cond
    (undefined? v) "undefined"
    (nil? v) "nil"
    :else (pr-str v)))

(defn throw-report
  [value {{:keys [file line column]} :meta
          :keys [expected message]}]
  (let [message (or message
                    (str "Expected " (readable-value expected) " "
                         "but received " (readable-value value)))
        fly (js/Error. message)]
    (js/Error.captureStackTrace fly "todo-app.vitest$is")
    (set! (.-cljsLocation fly) #js {:file file :line line :col column})
    (set! (.-actual fly) (with-out-str (pprint value)))
    (set! (.-expected fly) (with-out-str (pprint expected)))
    (throw fly)))

(defn value-matcher
  [value-fn details-fn]
  (try
    (let [value (value-fn)]
      (if value
        #js {:pass true}
        (let [{:keys [expected] :as report} (details-fn)]
          #js {:pass false
               :expected expected
               :actual value
               :message (fn value-report [& _] (throw-report value report))})))
    (catch :default ex
      (let [{:keys [expected] :as report} (details-fn)]
        #js {:pass false
             :expected expected
             :actual ex
             :message (fn value-error-report [& _] (throw-report ex report))}))))

(defn equal-matcher
  [value details-fn]
  (let [{:keys [expected] :as details} (details-fn)]
    (if (= value expected)
      #js {:pass true}
      #js {:pass false
           :expected expected
           :actual value
           :message (fn equal-report [& _] (throw-report value details))})))

(vitest/expect.extend
 #js {:todo_app_value_matcher value-matcher
      :todo_app_equality_matcher equal-matcher})

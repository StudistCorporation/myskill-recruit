(ns todo-app.wrappers.exception
  (:require [jsonista.core :as json]
            [taoensso.telemere :as telemere])
  (:import [clojure.lang ExceptionInfo]))

(defn report-and-respond
  [ex]
  (telemere/error! ex)
  {:body (json/write-value-as-bytes {:message "Unexpected error"})
   :headers {"content-type" "application/json"}
   :status 500})

(defn wrap-exception
  [handler]
  (fn exception-catcher
    [request]
    (try
      (handler request)
      (catch ExceptionInfo ex
        (let [data (ex-data ex)]
          (case (:type data)
            :muuntaja/decode
            {:headers {"content-type" "application/json"}
             :status 400
             :body (json/write-value-as-bytes {:message "Invalid JSON"})}

            :reitit.coercion/request-coercion
            {:headers {"content-type" "application/json"}
             :status 400
             :body (json/write-value-as-bytes {:message "Invalid request"})}

            ;; else
            (report-and-respond ex))))
      (catch Throwable ex
        (report-and-respond ex)))))

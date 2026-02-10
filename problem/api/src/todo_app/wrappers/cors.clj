(ns todo-app.wrappers.cors)

(def ^:private allowed-headers
  "Accept,Content-Type,Origin")

(def ^:private allowed-methods
  "GET,POST,PUT,DELETE,OPTIONS")

(def wrap-cors
  {:name ::cors
   :compile
   (fn [_data _opts]
     (fn cors-wrapper [handler]
       (fn cors-handler [{method :request-method :as request}]
         (let [origin (get-in request [:headers "origin"])
               cors-headers
               (cond-> {"Access-Control-Allow-Headers" allowed-headers
                        "Access-Control-Allow-Methods" allowed-methods}
                 origin
                 (assoc "Access-Control-Allow-Origin" origin))]
           (if (not= method :options)
             (update (handler request) :headers #(merge cors-headers %))
             {:status 204
              :headers cors-headers})))))})

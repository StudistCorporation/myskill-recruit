(ns todo-app.state.http.effects
  (:require [re-frame.core :as rf]))

(rf/reg-fx
 ::http
 (fn [{:keys [uri on-success on-error opts]}]
   (-> (js/fetch
        uri
        (clj->js (merge {:redirect :error
                         :signal (js/AbortSignal.timeout 5000)
                         :cache :reload}
                        opts)))
       (.then (fn [response]
                (if (<= 200 (.-status response) 299)
                  (rf/dispatch (conj on-success response))
                  (rf/dispatch (conj on-error response)))))
       (.catch #(rf/dispatch (conj on-error %))))))

(rf/reg-fx
 ::json
 (fn [{:keys [^js/Response response] {:keys [on-success]} :opts}]
   (-> (.json response)
       (.then (fn [data] (rf/dispatch (conj on-success (js->clj data :keywordize-keys true)))))
       (.catch (fn [_error] nil)))))

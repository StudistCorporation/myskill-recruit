(ns todo-app.state.http.events
  (:require
   [todo-app.router.api :as router]
   [todo-app.state.http.effects :as-alias fx]
   [malli.core :as m]
   [malli.transform :as mt]
   [re-frame.core :as rf]
   [reitit.core :as reitit]))

(def allowed-methods
  #{:delete :get :head :patch :post :put})

(def client-transformer
  (mt/transformer
   mt/string-transformer
   mt/strip-extra-keys-transformer))

(rf/reg-event-fx
 ::parse-json
 (fn [_ [_ opts response]]
   {:fx [[::fx/json {:response response
                     :opts opts}]]}))

(defn- build-uri
  [path query]
  (let [uri (js/URL. js/window.location)]
    (set! (.-pathname uri) path)
    (doseq [[k v] query]
      (when (some? v)
        (.append (.-searchParams uri) (name k) (str v))))
    uri))

(defn fetch
  [_ [_ route {:keys [on-error on-success params]
               {:keys [method] :as opts} :opts}]]
  (if-let [match (reitit/match-by-name! router/router route params)]
    (let [select-method? (if method #{method} allowed-methods)
          endpoints
          (reduce-kv
           (fn [a k v]
             (if (and (select-method? k) (some? v))
               (assoc a k v)
               a))
           {}
           (:result match))]
      (if (= 1 (count endpoints))
        (let [endpoint (first (vals endpoints))
              method (:method endpoint)
              path (:path match)
              body (when-let [expected (-> endpoint :data :parameters :body)]
                     (m/coerce expected params
                               client-transformer
                               identity))
              query (when-let [expected (-> endpoint :data :parameters :query)]
                      (m/coerce expected params
                                client-transformer
                                identity))
              http-args {:on-error (or on-error [::log-error])
                         :on-success on-success
                         :uri (build-uri path query)
                         :opts (cond-> (merge opts {:method method})
                                 (some? body) (assoc :body body))}]
          {:fx [[:dispatch [::request http-args]]]})
        (throw (js/Error.
                (str "Route supports multiple methods: " route)))))
    (throw (js/Error.
            (str "No such route " route " " params)))))

(rf/reg-event-fx ::fetch fetch)

(rf/reg-event-fx
 ::log-error
 (fn [_ [_ error]]
   (js/console.error "HTTP error:" error)
   {}))

(defn- with-json-body
  [{:keys [json?] {:keys [body]} :opts :as opts}]
  (if (and json? (some? body) (not (string? body)))
    (-> opts
        (update-in [:opts :body] #(js/JSON.stringify (clj->js %)))
        (update-in [:opts :headers "Content-Type"] #(or % "application/json")))
    opts))

(defn- wrap-json-response
  [{:keys [json? on-success] :as opts}]
  (if (and json? on-success)
    (assoc opts :on-success [::parse-json opts])
    opts))

(rf/reg-event-fx
 ::request
 (fn [_ [_ opts]]
   {:fx [[::fx/http (-> opts
                        (assoc :json? true)
                        (with-json-body)
                        (wrap-json-response))]]}))

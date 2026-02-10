(ns todo-app.css
  (:require [clojure.java.io :as io]
            [shadow.css.build :as cb]
            [shadow.cljs.devtools.server.fs-watch :as fs-watch]))

(defonce css-ref (atom nil))
(defonce css-watch-ref (atom nil))

(def custom-colors
  {"white"
   {"-50" "#ffffff"
    "-100" "#fcfcfd"
    "-300" "#f7f8f9"}
   "black"
   {"-400" "#5c6574"
    "-500" "#374050"
    "-600" "#121827"}
   "gray"
   {"-100" "#f1f3f5"
    "-200" "#e6e9ed"
    "-300" "#dadee5"
    "-400" "#d0d6df"
    "-500" "#c5cdda"
    "-600" "#a8b3c4"
    "-700" "#8b98ac"
    "-800" "#778394"
    "-900" "#676d79"}
   "blue"
   {"-100" "#e3f4fe"
    "-200" "#b8e5fc"
    "-300" "#4db7f3"
    "-400" "#0e9df0"
    "-500" "#1a8bf0"
    "-600" "#1e6ee8"
    "-700" "#2250e4"
    "-800" "#18349f"
    "-900" "#0f1d5c"}
   "red"
   {"-100" "#ffecec"
    "-400" "#f77474"
    "-500" "#d52727"
    "-700" "#a60707"}
   "green"
   {"-100" "#ebf5e9"
    "-400" "#73d260"
    "-500" "#25af0a"
    "-700" "#158000"}})

(def custom-aliases
  {:font-sans {:font-family "Noto Sans, Noto Sans JP, Helvetica Neue, sans-serif"}
   :text-xs {:font-size "0.75rem"}
   :text-sm {:font-size "0.875rem"}
   :text-base {:font-size "1rem"}
   :text-lg {:font-size "1.125rem"}
   :text-xl {:font-size "1.25rem"}
   :leading-normal {:line-height "1.6"}
   :shadow-sm {:box-shadow "0 2px 8px 0 rgba(0, 0, 0, 0.16)"}
   :shadow-md {:box-shadow "0 4px 12px 0 rgba(0, 0, 0, 0.16)"}})

(defn- apply-colors
  [build-state]
  (-> build-state
      (update :colors merge custom-colors)
      (cb/generate-color-aliases)))

(defn- apply-aliases
  [build-state]
  (reduce-kv
   (fn [state key value]
     (assoc-in state [:aliases key] value))
   build-state
   custom-aliases))

(defn generate-css
  ([] (generate-css {}))
  ([{:keys [minify?]}]
   (let [result
         (-> @css-ref
             (apply-aliases)
             (apply-colors)
             (cb/generate '{:main {:include [todo-app*]}})
             (cond-> minify? (cb/minify))
             (cb/write-outputs-to (io/file "web" "target" "build")))]
     (binding [*out* (io/writer System/out)]
       (println [:css] "Generated CSS")
       (doseq [mod (:outputs result)
               {:keys [warning-type] :as warning} (:warnings mod)]
         (prn [:css] (name warning-type) (dissoc warning :warning-type)))
       (println)))))

(defn watch
  {:shadow/requires-server true}
  []
  (reset! css-ref
          (-> (cb/start)
              (cb/index-path (io/file "web" "src" "todo_app") {})))
  (generate-css)
  (reset! css-watch-ref
          (fs-watch/start
           {}
           [(io/file "web" "src" "todo_app")]
           ["cljs"]
           (fn [updates]
             (try
               (doseq [{:keys [file event]} updates
                       :when (not= event :del)]
                 (swap! css-ref cb/index-file file))
               (generate-css)
               (catch Throwable e
                 (binding [*out* (io/writer System/out)]
                   (prn [:css] :build-failure e)))))))
  ::started)

(defn stop
  []
  (when-some [css-watch @css-watch-ref]
    (fs-watch/stop css-watch)
    (reset! css-ref nil))
  ::stopped)

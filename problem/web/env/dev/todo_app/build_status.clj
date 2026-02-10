(ns todo-app.build-status
  (:require [clojure.java.io :as io]))

(def marker-file (io/file "web/target/.first-build-completed"))
(def builds-to-complete #{:web :vitest})
(def completed-builds (atom #{}))

(defn build-hook
  {:shadow.build/stages #{:flush}}
  [build-state & _args]
  (let [build-id (:shadow.build/build-id build-state)]
    (swap! completed-builds conj build-id)
    (when (= @completed-builds builds-to-complete)
      (.mkdirs (.getParentFile marker-file))
      (spit marker-file "shadow-cljs build completed")
      (println "[:build-hook] All builds completed!")
      (reset! completed-builds #{}))
    build-state))

(defn html-hook
  {:shadow.build/stages #{:flush}}
  [build-state & _args]
  (let [src (io/file "web/resources/public/index.html")
        dest (io/file "web/target/build/index.html")]
    (when (.exists src)
      (.mkdirs (.getParentFile dest))
      (io/copy src dest)))
  build-state)

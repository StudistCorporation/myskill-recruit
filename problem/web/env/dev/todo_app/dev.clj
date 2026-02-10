(ns todo-app.dev
  (:require [todo-app.css :as css]
            [shadow.cljs.devtools.api :as shadow]))

(defn ^:export watch
  {:shadow/requires-server true}
  [& _]
  (shadow/watch :web)
  (css/watch)
  (shadow/watch :vitest))

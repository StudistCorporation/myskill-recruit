(ns todo-app.hooks
  (:require [clj-kondo.hooks-api :as api]))

(defn children->map
  [[& {:as node-map}]]
  (zipmap (map api/sexpr (keys node-map))
          (vals node-map)))

(defn rewrite-defquery
  [{:keys [node]}]
  (let [[[_ query-name & query-meta] [args] [body opts]]
        (partition-by api/vector-node? (:children node))
        {:keys [transform]} (children->map (:children opts))
        extended-args (update args :children #(cons (api/token-node '_) %))
        node-body (api/list-node
                   (list
                    extended-args
                    (if transform
                      (api/list-node
                       (list
                        transform
                        body))
                      body)))]
    {:node
     (api/list-node
      (-> [(api/token-node 'defn) query-name]
          (into query-meta)
          (conj
           (api/list-node
            (list
             args
             (api/list-node
              (cons
               query-name
               (cons
                (api/map-node {})
                (:children args)))))))
          (conj node-body)))}))

(ns todo-app.config
  (:require [cprop.source :refer [from-env]]
            [mount.core :refer [defstate]]))

(defstate config
  :start (from-env))

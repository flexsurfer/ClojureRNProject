(ns clojurernproject.events
  (:require [steroid.fx :as fx]))

(fx/defn
  init-app-db
  {:events [:init-app-db]}
  [_]
  {:db {:counter 0
        :delta   10}})

(fx/defn
  update-counter
  {:events [:update-counter]}
  [{:keys [db]}]
  {:db (update db :counter inc)})
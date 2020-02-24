(ns clojurernproject.subs
  (:require [steroid.subs :as subs]
            [re-frame.core :as re-frame]))

(subs/reg-root-subs #{:counter :delta})

(re-frame/reg-sub
 :counter-with-delta
 :<- [:counter]
 :<- [:delta]
 (fn [[counter delta]]
   (+ counter delta)))
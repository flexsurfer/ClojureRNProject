(ns clojurernproject.core
  (:require [steroid.rn.core :as rn]
            [re-frame.core :as re-frame]
            [steroid.rn.navigation.core :as rnn]
            [steroid.rn.navigation.stack :as stack]
            [steroid.rn.navigation.bottom-tabs :as bottom-tabs]
            [clojurernproject.views :as screens]
            [steroid.rn.navigation.safe-area :as safe-area]
            steroid.rn.navigation.events
            clojurernproject.events
            clojurernproject.subs))

(defn main-screens []
  [bottom-tabs/bottom-tab
   [{:name      :home
     :component screens/home-screen}
    {:name      :basic
     :component screens/basic-screen}
    {:name      :ui
     :component screens/ui-screen}
    {:name      :list
     :component screens/list-screen}
    {:name      :storage
     :component screens/storage-screen}]])

(defn root-stack []
  [safe-area/safe-area-provider
   [(rnn/create-navigation-container-reload
     {:on-ready #(re-frame/dispatch [:init-app-db])}
     [stack/stack {:mode :modal :header-mode :none}
      [{:name      :main
        :component main-screens}
       {:name      :modal
        :component screens/modal-screen}]])]])

(defn init []
  (rn/register-comp "ClojureRNProject" root-stack))
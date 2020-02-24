(ns clojurernproject.core
  (:require [steroid.rn.core :as rn]
            [steroid.views :as views]
            [re-frame.core :as re-frame]
            [re-frisk-rn.core :as rfr]
            [steroid.rn.navigation.core :as rnn]
            [steroid.rn.navigation.stack :as stack]
            [clojurernproject.views :as screens]
            steroid.rn.navigation.events
            clojurernproject.events
            clojurernproject.subs))

(views/defview root-stack []
  (views/letsubs [[navigator screen] (stack/create-stack-navigator)
                  home-comp (rn/reload-comp screens/home-screen)
                  modal-comp (rn/reload-comp screens/modal-screen)]
    {:component-did-mount (rnn/create-mount-handler #(re-frame/dispatch [:init-app-db]))}
    [rnn/navigation-container {:ref rnn/nav-ref-handler}
     [navigator {:mode :modal}
      [screen {:name      :home
               :component home-comp}]
      [screen {:name      :modal
               :component modal-comp}]]]))

(defn init []
  (rfr/enable)
  (rn/register-comp "ClojureRNProject" root-stack))
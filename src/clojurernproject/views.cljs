(ns clojurernproject.views
  (:require [steroid.rn.core :as rn]
            [steroid.views :as views]
            [re-frame.core :as re-frame]))

(views/defview home-screen []
  (views/letsubs [counter [:counter-with-delta]]
    [rn/view {:style {:align-items :center :justify-content :center :flex 1}}
     [rn/text (str "Counter with delta: " counter)]
     [rn/touchable-opacity {:on-press #(re-frame/dispatch [:update-counter])}
      [rn/view {:style {:background-color :gray :padding 5}}
       [rn/text "Update counter"]]]
     [rn/touchable-opacity {:on-press #(re-frame/dispatch [:navigate-to :modal])
                            :style {:margin-top 20}}
      [rn/view {:style {:background-color :gray :padding 5}}
       [rn/text "Open modal"]]]]))

(defn modal-screen []
  [rn/view {:style {:align-items :center :justify-content :center :flex 1}}
   [rn/touchable-opacity {:on-press #(re-frame/dispatch [:navigate-back])}
    [rn/view {:style {:background-color :gray :padding 5}}
     [rn/text "Navigate back"]]]])
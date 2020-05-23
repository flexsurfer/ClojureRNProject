(ns clojurernproject.views
  (:require [steroid.rn.core :as rn]
            [steroid.views :as views]
            [re-frame.core :as re-frame]
            [steroid.rn.components.ui :as ui]
            [reagent.core :as reagent]
            [steroid.rn.navigation.safe-area :as safe-area]
            #_[steroid.rn.components.picker :as picker]
            [steroid.rn.components.list :as list]
            #_[steroid.rn.components.async-storage :as async-storage]
            #_[steroid.rn.components.datetimepicker :as datetimepicker]
            [clojure.string :as string]))

(views/defview home-screen []
  (views/letsubs [counter [:counter-with-delta]]
    [safe-area/safe-area-view {:style {:flex 1}}
     [rn/view {:style {:align-items :center :justify-content :center :flex 1}}
      [rn/text {:style {:margin-bottom 20}}
       (str "Counter with delta: " counter)]
      [ui/button {:title    "Update counter"
                  :on-press #(re-frame/dispatch [:update-counter])}]
      [rn/view {:style {:height 20}}]
      [ui/button {:title    "Basic components"
                  :on-press #(re-frame/dispatch [:navigate-to :basic])}]
      [ui/button {:title    "UI components"
                  :on-press #(re-frame/dispatch [:navigate-to :ui])}]
      [ui/button {:title    "List"
                  :on-press #(re-frame/dispatch [:navigate-to :list])}]
      [ui/button {:title    "Storage"
                  :on-press #(re-frame/dispatch [:navigate-to :storage])}]
      [ui/button {:title    "Open modal"
                  :on-press #(re-frame/dispatch [:navigate-to :modal])}]]]))

(defn modal-screen []
  [rn/view {:style {:align-items :center :justify-content :center :flex 1}}
   [ui/button {:title    "Navigate back"
               :on-press #(re-frame/dispatch [:navigate-back])}]])

(defn basic-screen []
  [safe-area/safe-area-view
   [rn/view {:style {:padding 20}}
    ;;VIEW
    [rn/view {:style {:width 300 :height 20 :background-color :blue}}]
    ;;TEXT
    [rn/text {:style {:color :red :font-size 30 :margin-top 10}} "Text2"]
    ;;IMAGE
    [rn/image {:style  {:width 50 :height 50 :margin-top 10}
               :source {:uri "https://reactnative.dev/img/tiny_logo.png"}}]
    ;;TEXT INPUT
    [rn/text-input {:style       {:height     40 :borderColor :gray :border-width 1
                                  :margin-top 10}
                    :placeholder "Type your text ..."}]
    ;;SCROLL VIEW
    [rn/scroll-view {:style {:height 100 :margin-top 10}}
     (for [i (range 20)]
       ^{:key (str "item" i)}
       [rn/view {:style {:margin-bottom    5 :width 300 :height 20
                         :background-color (str "#" (.toString (rand-int 16rFFFFFF) 16))}}])]]])

(defn ui-screen []
  (let [enabled?     (reagent/atom false)
        picker-value (reagent/atom "clj")]
    (fn []
      [safe-area/safe-area-view
       [rn/view {:style {:padding 20}}
        ;;BUTTON
        [ui/button {:title "Button"}]
        [ui/button {:title "Button" :color :red}]
        ;;SWITCH
        [ui/switch {:on-value-change #(swap! enabled? not)
                    :value           @enabled?}]
        [rn/text {:style {:margin-top 10}}
         (str "Switch ebabled: " @enabled?)]
        ;;PICKER
        [rn/text "add \"@react-native-community/picker\" and uncomment the code"]
        #_[picker/picker {:style           {:height 250 :width 200}
                          :selected-value  @picker-value
                          :on-value-change #(reset! picker-value %)}
           [picker/item {:label "Clojure" :value "clj"}]
           [picker/item {:label "ClojureScript" :value "cljs"}]
           [picker/item {:label "Java" :value "java"}]
           [picker/item {:label "JavaScript" :value "js"}]]
        [rn/text (str "Selected item: " @picker-value)]
        ;;DATETIME PICKER
        [rn/text "add \"@react-native-community/datetimepicker\" and uncomment the code"]
        #_[datetimepicker/date-time-picker
           {:testID "dateTimePicker"
            :timeZoneOffsetInMinutes 0
            :value (js/Date. 1598051730000)
            :is24Hour true
            :display "default"}]]])))

(defn flat-list-renderer [{:keys [title value]}]
  [rn/text (str title " " value)])

(defn section-list-renderer [{:keys [title value]}]
  [rn/text (str title " " value)])

(defn list-screen []
  [safe-area/safe-area-view
   [rn/view {:style {:padding 20}}
    ;;FLAT LIST
    [list/flat-list {:style     {:height 100}
                     :data      (for [i (range 20)]
                                  {:title "item"
                                   :value i})
                     :render-fn flat-list-renderer}]
    ;;SECTION LIST
    [list/section-list {:style     {:height     100
                                    :margin-top 50}
                        :sections  [{:title "Title 1"
                                     :key   :data1
                                     :data  (for [i (range 10)]
                                              {:title "item 1"
                                               :value i})}
                                    {:title "Title 2"
                                     :key   :data2
                                     :data  (for [i (range 10)]
                                              {:title "item 2"
                                               :value i})}
                                    {:title "Title 3"
                                     :key   :data3
                                     :data  (for [i (range 10)]
                                              {:title "item 3"
                                               :value i})}]
                        :render-fn section-list-renderer}]]])

(defn storage-screen []
  (let [read-value (reagent/atom "")
        new-value  (reagent/atom "")]
    (fn []
      [safe-area/safe-area-view
       [rn/text "add \"@react-native-community/async-storage\" and uncomment the code"]
       #_[rn/view {:style {:padding 20}}
          [ui/button {:title    "Read value"
                      :on-press (fn []
                                  (async-storage/get-item "my-key" #(reset! read-value %)))}]
          [rn/text (str "Read value " @read-value)]
          [rn/text-input {:style          {:height      40 :margin-top 10
                                           :borderColor :gray :border-width 1}
                          :on-change-text #(reset! new-value %)
                          :placeholder    "Type your text ..."}]
          [ui/button {:title    "Write value"
                      :disabled (string/blank? @new-value)
                      :on-press #(async-storage/set-item "my-key" @new-value)}]]])))

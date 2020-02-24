hackmd: https://hackmd.io/@byc70E6fQy67hPMN0WM9_A/rJilnJxE8

# Confidence and Joy: React Native Development with ClojureScript and re-frame

Clojure: https://clojure.org/guides/getting_started

Code editor: IntelliJ IDEA Community https://www.jetbrains.com/idea/download/
with Cursive plugin https://cursive-ide.com/

shadow-cljs: http://shadow-cljs.org/
re-frame-steroid: https://github.com/flexsurfer/re-frame-steroid
rn-shadow-steroid: https://github.com/flexsurfer/rn-shadow-steroid

### 1. Create a new React Native Project or open existing one

`react-native init ClojureRNProject`

`cd ClojureRNProject`

Open project in IDE

![](https://i.imgur.com/GFLzmOi.png )

Edit `App.js`

```jsx=
import React from 'react';
import {
  SafeAreaView,
  View,
  Text,
} from 'react-native';

const App: () => React$Node = () => {
  return (
    <>
      <SafeAreaView>
        <View>
        <Text>Hello CLojure!</Text>
        </View>
      </SafeAreaView>
    </>
  );
};

export default App;
```

Run the app

Terminal 1: `yarn start`
Terminal 2: `yarn ios`

![](https://i.imgur.com/uO6xvCK.png )


OK, now we have RN project and we want to run the same app but with clojure


### 2. Add shadow-cljs

`yarn add --dev shadow-cljs`
`yarn global add shadow-cljs`

Create `shadow-cljs.edn`

```clojure
{:source-paths ["src"]

 :dependencies [[reagent "0.9.1" :exclusions [cljsjs/react cljsjs/react-dom]]
                [re-frame "0.11.0"]
                [re-frame-steroid "0.1.1"]
                [rn-shadow-steroid "0.1.1"]
                [re-frisk-rn "0.1.0"]]

 :builds       {:dev
                {:target     :react-native
                 :init-fn    clojurernproject.core/init
                 :output-dir "app"
                 :devtools   {:autoload true
                              :after-load steroid.rn.core/reload}}}}
```

Next, we need to initialize project as Clojure Deps, it will be used only for code inspection in IDE, if you know a better way pls file a PR

### 3. Create cljs project

create `deps.edn` file

```clojure
{:deps  {org.clojure/clojure       {:mvn/version "1.10.0"}
         org.clojure/clojurescript {:mvn/version "1.10.339"}
         reagent                   {:mvn/version "0.9.1"}
         re-frame                  {:mvn/version "0.11.0"}
         re-frame-steroid          {:mvn/version "0.1.1"}
         rn-shadow-steroid         {:mvn/version "0.1.1"}
         re-frisk-rn               {:mvn/version "0.1.0"}}
 :paths ["src"]}
```

Right click on the file and `Add as Clojure Deps Project`

![](https://i.imgur.com/C110quU.png)

Optional turn off a spelling

Indellij IDEA -> Preferences

![](https://i.imgur.com/eqWzrqM.png)

create `src` folder and `clojurernproject` package with `core.cljs` file

![](https://i.imgur.com/gDEWfL3.png)


core.cljs

```clojure
(ns clojurernproject.core
  (:require [steroid.rn.core :as rn]))

(defn root-comp []
  [rn/safe-area-view
   [rn/view
    [rn/text "Hello CLojure! from CLJS"]]])

(defn init []
  (rn/register-reload-comp "ClojureRNProject" root-comp))

```

index.js

```javascript=
import "./app/index.js";
```

Terminal 3: `shadow-cljs watch dev`

Reload the app

**Disable Fast Refresh**

Cmnd+D

![](https://i.imgur.com/7sOO4Ko.png )

Now try to change the code, you should see it reloaded by shadow-cljs in the app

Congrats now you have clojurescript RN app configured with hot reload


### 4. App state with re-frame

To update app state, we need to use events, let's create `events.cljs` and register the first event

events.cljs
```clojure
(ns clojurernproject.events
  (:require [steroid.fx :as fx]))

(fx/defn
  init-app-db
  {:events [:init-app-db]}
  [_]
  {:db {:counter 0}})

(fx/defn
  update-counter
  {:events [:update-counter]}
  [{:keys [db]}]
  {:db (update db :counter inc)})
```

To update a view when the state is changed, we need to use subscriptions, let's create `subs.cljs` and register subscriptions.

subs.cljs
```clojure
(ns clojurernproject.subs
  (:require [steroid.subs :as subs]))

(subs/reg-root-subs #{:counter})
```

Now we can update our view

core.cljs
```clojure
(ns clojurernproject.core
  (:require [steroid.rn.core :as rn]
            [steroid.views :as views]
            [re-frame.core :as re-frame]
            clojurernproject.events
            clojurernproject.subs))

(views/defview root-comp []
  (views/letsubs [counter [:counter]]
    [rn/safe-area-view {:style {:flex 1}}
     [rn/view {:style {:align-items :center :justify-content :center :flex 1}}
      [rn/text (str "Counter: " counter)]
      [rn/touchable-opacity {:on-press #(re-frame/dispatch [:update-counter])}
       [rn/view {:style {:background-color :gray :padding 5}}
        [rn/text "Update counter"]]]]]))

(defn init []
  (re-frame/dispatch [:init-app-db])
  (rn/register-reload-comp "ClojureRNProject" root-comp))
```

Now you can press "Update counter" button, and then change your code, and you can see app updated, but app state remained the same

![](https://i.imgur.com/T5wfvnX.png)

Congrats now you have clojurescript RN app configured with hot reload and re-frame state

There are three major rules when working with re-frame
1) views are pure and dumb, just show data from subscriptions and dispatch events

Bad: 
```clojure
(views/defview comp []
  (views/letsubs [counter [:counter]
                  delta [:delta]]
    [rn/text (str "Counter: " (+ counter delta))]
    [rn/touchable-opacity 
     {:on-press #(re-frame/dispatch 
                  [:update-counter (if (> delta 12) 
                                     counter 
                                     delta)])}]))
```

Good: 
```clojure
(views/defview comp []
  (views/letsubs [counter-with-delta [:counter-with-delta]]
    [rn/text (str "Counter: " counter-with-delta)]
    [rn/touchable-opacity 
     {:on-press #(re-frame/dispatch 
                  [:update-counter])}]))
```
we have a separate subscription and event will get all data from state

2. Only root keys should be subscribed on app-db

Bad:
```clojure
(re-frame/reg-sub :counter (fn [db] (get db :counter)))

(re-frame/reg-sub :delta (fn [db] (get db :delta)))

(re-frame/reg-sub :counter-with-delta (fn [db] (+ (get db :counter) (get db :delta)))
```

Good:

```clojure
(subs/reg-root-subs #{:counter :delta})

(re-frame/reg-sub
 :counter-with-delta
 :<- [:counter]
 :<- [:delta]
 (fn [[counter delta]]
   (+ counter delta)))
```

3. Events must be pure and do all computations

Bad:
```clojure
(fx/defn
  update-counter
  {:events [:update-counter]}
  [{:keys [db]}]
  (do-something)
  {:db (update db :counter inc)})
```

Good:
```clojure
(re-frame/reg-fx
  :do-something
  (fn []
    (do-something)))

(fx/defn
  update-counter
  {:events [:update-counter]}
  [{:keys [db]}]
  {:db (update db :counter inc)
   :do-something nil})
```

### 6. Devtools

let's add re-frisk debugging tool and see what's exactly happening in the app

core.cljs

```clojure
(ns clojurernproject.core
  (:require ....
            [re-frisk-rn.core :as rfr]))

(defn init []
  (rfr/enable)
  (re-frame/dispatch [:init-app-db])
  (rn/register-reload-comp "ClojureRNProject" root-comp))
```

And run the tool

Terminal 4: `shadow-cljs run re-frisk-rn.core/start`

And open `http://localhost:4567`

![](https://i.imgur.com/6ty7nbr.png)

You can see all that is happening with the app: events, app-db (state) and subscriptions

### 6. Tests

Add test folder and configure test build in project 

```clojure
{:source-paths ["src" "test"]

 :dependencies [[...]]

 :builds       {:dev
                {...}

                :test
                {:target    :node-test
                 :output-to "out/node-tests.js"
                 :autorun   true}}}
```

Let's add some tests

events/counter_test.cljs

```clojure
(ns events.counter-test
  (:require [cljs.test :refer (deftest is)]
            [clojurernproject.events :as events]))

(deftest events-counter-test
  (is (= (events/update-counter {:db {:counter 0}})
         {:db {:counter 1}})))
```

And run tests

Terminal 3: `shadow-cljs compile test`

![](https://i.imgur.com/28gspBL.png)

### 7. Navigation

React Navigation 5

Terminal 2: `yarn add @react-navigation/native @react-navigation/stack react-native-reanimated react-native-gesture-handler react-native-screens react-native-safe-area-context @react-native-community/masked-view`


Terminal 2: `cd ios; pod install; cd ..`

Terminal 2: `yarn ios`

core.cljs
```clojure
(ns clojurernproject.core
  (:require [steroid.rn.core :as rn]
            [steroid.views :as views]
            [re-frame.core :as re-frame]
            [re-frisk-rn.core :as rfr]
            [steroid.rn.navigation.core :as rnn]
            [steroid.rn.navigation.stack :as stack]
            steroid.rn.navigation.events
            clojurernproject.events
            clojurernproject.subs))

(views/defview home-screen []
  (views/letsubs [counter [:counter-with-delta]]
    [rn/safe-area-view {:style {:flex 1}}
     [rn/view {:style {:align-items :center :justify-content :center :flex 1}}
      [rn/text (str "Counter with delta: " counter)]
      [rn/touchable-opacity {:on-press #(re-frame/dispatch [:update-counter])}
       [rn/view {:style {:background-color :gray :padding 5}}
        [rn/text "Update counter"]]]
      [rn/touchable-opacity {:on-press #(re-frame/dispatch [:navigate-to :modal])
                             :style {:margin-top 20}}
       [rn/view {:style {:background-color :gray :padding 5}}
        [rn/text "Open modal"]]]]]))

(defn modal-screen []
  [rn/view {:style {:align-items :center :justify-content :center :flex 1}}
    [rn/touchable-opacity {:on-press #(re-frame/dispatch [:navigate-back])}
     [rn/view {:style {:background-color :gray :padding 5}}
      [rn/text "Navigate back"]]]])

(views/defview root-stack []
  (views/letsubs [[navigator screen] (stack/create-stack-navigator)
                  home-comp (rn/reload-comp home-screen)
                  modal-comp (rn/reload-comp modal-screen)]
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
```

For hot reload we need to register components differently, we register `root-stack` as regular not reloadable component `rn/register-comp` but we use `rn/reload-comp` for screens, it's important to init screen components outside renderer 

After we required `steroid.rn.navigation.events` ns and added `nav-ref-handler`, we can dispatch `:navigate-to` and `:navigate-back` events to navigate between screens

Try to open modal screen and change the code you will see that navigation state isn't changed, the modal screen will be still opened

![](https://i.imgur.com/RUoATTt.png)


КОНЕЦ
hackmd: https://hackmd.io/@byc70E6fQy67hPMN0WM9_A/rJilnJxE8

# Confidence and Joy: React Native Development with ClojureScript and re-frame

Clojure: https://clojure.org/guides/getting_started

Code editor: IntelliJ IDEA Community https://www.jetbrains.com/idea/download/
with Cursive plugin https://cursive-ide.com/

shadow-cljs: http://shadow-cljs.org/
re-frame-steroid: https://github.com/flexsurfer/re-frame-steroid
rn-shadow-steroid: https://github.com/flexsurfer/rn-shadow-steroid

PROJECT SOURCES: https://github.com/flexsurfer/ClojureRNProject

### 1. Create a new React Native Project or open existing one

`react-native init ClojureRNProject`

`cd ClojureRNProject`

Open project in IDE

![](https://i.imgur.com/GFLzmOi.png)

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

![](https://i.imgur.com/uO6xvCK.png)


OK, now we have RN project and we want to run the same app but with clojure


### 2. Add shadow-cljs

`yarn add shadow-cljs`

If you already have it, make sure you are using the latest version

Create `shadow-cljs.edn`

```clojure
{:source-paths ["src"]

 :dependencies [[reagent "0.10.0"]
                [re-frame "0.12.0"]
                [re-frame-steroid "0.1.1"]
                [rn-shadow-steroid "0.2.1"]
                [re-frisk-remote "1.3.3"]]

 :builds       {:dev
                {:target     :react-native
                 :init-fn    clojurernproject.core/init
                 :output-dir "app"
                 :compiler-options {:closure-defines
                                    {"re_frame.trace.trace_enabled_QMARK_" true}}
                 :devtools   {:after-load steroid.rn.core/reload
                              :build-notify steroid.rn.core/build-notify
                              :preloads [re-frisk-remote.preload]}}}}
```

Next, we need to initialize project as Clojure Deps, `deps.edn` will be used only for code inspection in IDE, if you know a better way pls file a PR

### 3. Create cljs project

create `deps.edn` file

```clojure
{:deps  {org.clojure/clojure       {:mvn/version "1.10.0"}
         org.clojure/clojurescript {:mvn/version "1.10.339"}
         reagent                   {:mvn/version "0.10.0"}
         re-frame                  {:mvn/version "0.12.0"}
         re-frame-steroid          {:mvn/version "0.1.1"}
         rn-shadow-steroid         {:mvn/version "0.2.1"}}
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

![](https://i.imgur.com/7sOO4Ko.png)

Now try to change the code, you should see it reloaded by shadow-cljs in the app

now you have clojurescript RN app configured with hot reload


### 4. App state with re-frame

To update app state, we need to use events, let's create `events.cljs` and register our first events

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

Set cursor on `fx/defn` and press `option+return` 

![](https://i.imgur.com/4ahMkVJ.png)

Move selection to `Resolve .. as...` and press `return` then select `defn`

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

Resolve `defview` as `defn` and `letsubs` as `let` same way how we did it for `fx/defn`

you can press "Update counter" button, and then change your code, and you can see app updated, but app state remained the same

![](https://i.imgur.com/T5wfvnX.png)

now you have clojurescript RN app configured with hot reload and re-frame state

There are three major rules when working with re-frame
1) views are pure and dumb, just render UI with data from subscriptions and dispatch events

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
     {:on-press #(re-frame/dispatch [:update-counter])}]))
```
we have a separate subscription and event will get all data from the state

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

let's run re-frisk debugging tool and see what's exactly happening in the app

Terminal 4: `shadow-cljs run re-frisk-remote.core/start`

and open `http://localhost:4567`

![](https://i.imgur.com/6ty7nbr.png)

You can see all that is happening with the app: events, app-db (state) and subscriptions

### 6. Tests

Add test folder and configure test build in the project 

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

Terminal 2: `yarn add @react-navigation/native @react-navigation/stack @react-navigation/bottom-tabs react-native-reanimated react-native-gesture-handler react-native-screens react-native-safe-area-context @react-native-community/masked-view`

Terminal 2: `cd ios; pod install; cd ..`

Terminal 2: `yarn ios`

core.cljs
```clojure
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
```

For hot reload we need to register components differently, we register `root-stack` as regular not reloadable component `rn/register-comp` but we use `rnn/create-navigation-container-reload` for navigation container

After we've required `steroid.rn.navigation.events` ns we can dispatch `:navigate-to` and `:navigate-back` events for navigation between screens

Try to open modal screen and change the code you will see that navigation state isn't changed, the modal screen will be still opened

![IMG](https://github.com/flexsurfer/rn-shadow-steroid/raw/master/screencast.gif)

КОНЕЦ

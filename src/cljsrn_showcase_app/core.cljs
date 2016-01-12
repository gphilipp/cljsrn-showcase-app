(ns cljsrn-showcase-app.core
  (:require-macros [natal-shell.core :refer [with-error-view]]
                   [natal-shell.components :refer [view text image touchable-highlight navigator]]
                   [natal-shell.alert :refer [alert]])
  (:require [om.next :as om :refer-macros [defui]]))

(set! js/React (js/require "react-native/Libraries/react-native/react-native.js"))


(enable-console-print!)


;;;;;;;;;;;;;;;;;
;; Vector Icons
;;;;;;;;;;;;;;;;;

(def VectorIcon (js/require "react-native-vector-icons/index.js"))
;(defn vector-icon [opts] (js/React.createElement Icon (clj->js opts)))
(def FontAwesomeIcon (js/require "FontAwesome"))

;;;;;;;;;;;;;;;;;
;; Navigation Bar
;;;;;;;;;;;;;;;;;

(defn navigation-bar [opts & children]
  (apply js/React.createElement js/React.Navigator.NavigationBar (clj->js opts) children))


;;;;;;;;;;;;;;;;;
;; Tab Bar
;;;;;;;;;;;;;;;;;

(defn tab-bar-ios [opts & children]
  (apply js/React.createElement js/React.TabBarIOS (clj->js opts) children))

(defn tab-bar-item [opts & children]
  (apply js/React.createElement
         FontAwesomeIcon.TabBarItem (clj->js opts) children))




(def app-state (atom {:title "home"}))


(defui HomeComponent
  static om/IQuery
  (query [this]
    '[:title])
  Object
  (render [this]
    (let [navigator (om/get-computed this :navigator)
          _ (println "SettingsComponent got navigator:" navigator)]
      (view {:style {:margin 10 :height 600}}
            (text {:style {:textAlign "center" :fontSize 20 :fontWeight "200"}}
                  "Home Page 2")
            (touchable-highlight {:style {:alignSelf "center"
                                          :backgroundColor "#007aff"
                                          :marginTop 80
                                          :marginBottom 20
                                          :padding 10
                                          :borderRadius 5}
                                  :onPress (fn [event]
                                             (.push navigator
                                                    (clj->js {:name "settings"})))}
                                 (text {:style {:color "white"
                                                :textAlign "center"
                                                :fontWeight "bold"}}
                                       "Go settings"))))))

(def home-component (om/factory HomeComponent))


(defui SettingsComponent
  static om/IQuery
  (query [this]
    '[:title])
  Object
  (render [this]
    (let [navigator (om/get-computed this :navigator)
          _ (println "SettingsComponent got navigator:" navigator)]
      (view {:style {:margin 10 :height 600}}
            (text {:style {:textAlign "center" :fontSize 20 :fontWeight "200"}}
                  "Settings Page")
            (touchable-highlight {:style {:alignSelf "center"
                                          :backgroundColor "#007aff"
                                          :marginTop 80
                                          :marginBottom 20
                                          :padding 10
                                          :borderRadius 5}
                                  :onPress (fn [event]
                                             (.push navigator
                                                    (clj->js {:name "home"})))}
                                 (text {:style {:color "white"
                                                :textAlign "center"
                                                :fontWeight "bold"}}
                                       "Go to home"))))))

(def settings-component (om/factory SettingsComponent))


(defn mutate [{:keys [state] :as env} key params]
  (condp = key
    `store-navigator (let [{:keys [navigator]} params]
                       (do
                         (println "Store navigator: " navigator)
                         {:value {:keys [:title]}
                          :action #(swap! state assoc :navigator navigator)
                          })))
  )


(defui MainView
  static om/IQuery
  (query [this]
    '[:title])

  Object
  (render [this]
    (with-error-view
      (let [{:keys [title] :as props} (om/props this)
            _ (println "props in MainView" props)]
        (view {:style {:flex 1
                       :flexDirection "column"
                       :marginTop 50}}

              (text {:style {:fontSize 50 :fontWeight "100"
                             :marginBottom 20 :textAlign "center"}}
                    "Welcome 6!")

              #_(navigator {:initialRoute {:name "home" :title "Home Next" :index 1}
                          :ref (fn [navigator]
                                 (do (println "store reference of navigator in props")
                                     (om/transact! this [(`store-navigator {:navigator ~navigator})])
                                     (println "Now props:" (om/get-props this))
                                     ))
                          :renderScene (fn [route navigator]
                                         (let [props-with-navigator (om/computed props {:navigator navigator})
                                               _ (println "route:" route)
                                               _ (println "navigator passed to renderScene:" navigator)]
                                           (condp = (:name (js->clj route :keywordize-keys true))
                                             "home" (home-component props-with-navigator)
                                             "settings" (settings-component props-with-navigator))))})

              (tab-bar-ios {:style {:flex 0.5
                                    }}
                           (tab-bar-item
                             {:iconName "gamepad"
                              :title "Home"
                              :onPress #(alert "Navigate to home")})
                           (tab-bar-item


                             {:iconName "gear"
                              :title "Settings"
                              :onPress (fn [e]
                                         (do (println "event:" e)
                                             (let [navigator (om/get-computed this :navigator)]
                                               (alert (str "navigator in tabbi:" navigator))
                                               )))})))))))

(defmulti read om/dispatch)
(defmethod read :default
  [{:keys [state]} k _]
  (let [st @state]
    (if-let [[_ v] (find st k)]
      {:value v}
      {:value :not-found})))

(def reconciler
  (om/reconciler
    {:state app-state
     :parser (om/parser {:read read :mutate mutate})
     :root-render #(.render js/React %1 %2)
     :root-unmount #(.unmountComponentAtNode js/React %)}))

(om/add-root! reconciler MainView 1)

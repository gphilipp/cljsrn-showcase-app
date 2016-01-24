(ns cljsrn-showcase-app.core
    (:require-macros [natal-shell.core :refer [with-error-view]]
                     [natal-shell.components :refer [view text image touchable-highlight navigator]]
                     [natal-shell.alert :refer [alert]])
    (:require [om.next :as om :refer-macros [defui]]))

(set! js/React (js/require "react-native/Libraries/react-native/react-native.js"))


(def RNChart (.-default (js/require "react-native-chart/lib/index.js")))
(defn chart [opts]
    (js/React.createElement RNChart (clj->js opts)))

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


;;;;;;;;;;;;;;;;;
;; PickerIos
;;;;;;;;;;;;;;;;;

(defn picker-ios [opts & children]
    (apply js/React.createElement js/React.PickerIOS (clj->js opts) children))

(defn picker-item-ios [opts & children]
    (apply js/React.createElement js/React.PickerIOS.Item (clj->js opts) children))


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
                        "Home")
                  (picker-ios {:style {:height 40
                                       :flex 2
                                       ;:backgroundColor "yellow"
                                       :borderWidth 1}
                               :selectedValue (om/get-state this :selected-item)
                               :onValueChange #(om/update-state! this assoc :selected-item %)}
                              (picker-item-ios {:key "gcn"
                                                :value "gamecube"
                                                :label "Gamecube"
                                                })
                              (picker-item-ios {:key "gen"
                                                :value "genesis"
                                                :label "Genesis"
                                                }))
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



(defui StatsComponent
    Object
    (render [this]
        (let [navigator (om/get-computed this :navigator)]
            (view {:style {:flex 1}}

                  (text {:style {:textAlign "center" :fontSize 20 :fontWeight "200"}}
                        "Stats here")

                  (chart {:style {:position "absolute" :top 16 :left 4 :bottom 4 :right 16}
                          :chartTitle "My Simple Chart"
                          :chartFontSize 22
                          :xAxisTitle "X Axis"
                          :yAxisTitle "Y Axis"
                          :chartData [
                                      {:name "BarChart"
                                       :type "bar"
                                       :color "purple"
                                       :widthPercent 0.6
                                       :data [30 1 1 2 3 5 21 13 21 34 55 30]
                                       }

                                      {:name "LineChart"
                                       :color "gray"
                                       :lineWidth 2
                                       :showDataPoint false
                                       :data [10 12 14 25 31 52 41 31 52 66 22 11]}
                                      ]
                          :verticalGridStep 5
                          :xLabels ["0" "1" "2" "3" "4" "5" "6" "7" "8" "9" "10" "11"]
                          })
                  ))))

(def home-component (om/factory HomeComponent))
(def stats-component (om/factory StatsComponent))


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
                        "Settings")
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
                            "Welcome!")

                      (navigator {
                                  :initialRoute {:name "home" :title "Home" :index 0}
                                  :ref (fn [navigator]
                                           (om/update-state! this :navigator navigator)
                                           ;(om/transact! reconciler [(`store-navigator {:navigator ~navigator}) title])
                                           )
                                  :renderScene (fn [route navigator]
                                                   (let [props-with-navigator (om/computed props {:navigator navigator})
                                                         _ (println "route:" route)
                                                         _ (println "navigator passed to renderScene:" navigator)]
                                                       (condp = (:name (js->clj route :keywordize-keys true))
                                                           "home" (home-component props-with-navigator)
                                                           "stats" (stats-component props-with-navigator)
                                                           "settings" (settings-component props-with-navigator))))})

                      (let [navigate (fn [routename]
                                         #(let [navigator (om/get-state this)]
                                             (.push navigator
                                                    (clj->js {:name routename}))))]
                          (tab-bar-ios {:style {:flex 0.5
                                                }}
                                       (tab-bar-item
                                           {:iconName "gamepad"
                                            :title "Home"
                                            :onPress (navigate "home")})
                                       (tab-bar-item
                                           {:iconName "bar-chart"
                                            :title "Statistics"
                                            :onPress (navigate "stats")})
                                       (tab-bar-item
                                           {:iconName "gear"
                                            :title "Settings"
                                            :onPress (navigate "settings")}))))))))

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

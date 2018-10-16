(ns snek.main
    (:require [rum.core :as rum]
              [snek.engine :as engine]))

(enable-console-print!)

(println "This text is printed from src/snek/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(rum/defc hello-world []
  [:div
   [:h1 (:text @app-state)]
   [:h3 "Edit this and watch it change!"]])

(rum/mount (hello-world)
           (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

;; Register event-handlers that parse and push movement key-presses to channel
;; Engine listens to channel and reacts
;; Engine will delegate event to core and swap atom
;; Render each 30 ms or so based on current state

;; Possible that render will not be based on ms but a callback to state swap?

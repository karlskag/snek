(ns snek.main
  (:require [rum.core :as rum]
            [snek.engine :as engine]
            [cljs.core.async :refer [chan put!]]))

(enable-console-print!)

; CANVAS BEGIN

(def default-sizes {:height 10 :width 10})

(defn get-canvas [] (. js/document (getElementById "playArea")))

(defn get-context [canvas] (.getContext canvas "2d"))

(defn set-canvas-size
  [canvas width height]
  (set! (.-width canvas) width)
  (set! (.-height canvas) height))

(defn draw-rect
  [ctx [x y]]
  (.fillRect ctx x y (:height default-sizes) (:width default-sizes)))

; CANVAS END

(defn parse-event
  [event]
  (get {"ArrowUp"    :up
        "ArrowDown"  :down
        "ArrowLeft"  :left
        "ArrowRight" :right}
       (.-key event)))

(defn init-canvas
  []
  (let [canvas (get-canvas)
        ctx (get-context canvas)]
    (set-canvas-size canvas 500 500)
    (draw-rect ctx [50 50])))

(defn key-handler
  [chan event]
  (put! chan (parse-event event)))

(defonce app-state (atom nil))
(def event-chan (chan))
(js/window.addEventListener "keydown" (partial key-handler event-chan))
(init-canvas)
(engine/start app-state event-chan)

(rum/defc Root < rum/reactive []
  [:div "Player: " (get-in (rum/react app-state) [:player :coordinates])])

(rum/mount (Root)
           (. js/document (getElementById "app")))
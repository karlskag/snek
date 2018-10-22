(ns snek.main
  (:require [rum.core :as rum]
            [snek.engine :as engine]
            [cljs.core.async :refer [chan put!]]
            [snek.state :as s]))

(enable-console-print!)

; CANVAS BEGIN
(def colors {:snake "#000000" :food "#FF0000"})
(def default-sizes {:height 10 :width 10})
(def canvas (. js/document (getElementById "playArea")))
(def ctx (.getContext canvas "2d"))

(defn set-canvas-size
  [canvas width height]
  (set! (.-width canvas) width)
  (set! (.-height canvas) height))

(defn draw-rect
  [[x y]]
  (let [width (:width default-sizes)
        height (:height default-sizes)]
    (set! (.-fillStyle ctx) (:snake colors))
    (.fillRect ctx
               (* x width)
               (* y height)
               height
               width)))

(defn draw-circle
  [[x y]]
  (let [width (:width default-sizes)
        height (:height default-sizes)]
    (set! (.-fillStyle ctx) (:food colors))
    (doto ctx
      (.beginPath)
      (.arc (- (* x width) (/ width 2)) (- (* y height) (/ height 2)) 5 0 (* 2 js/Math.PI))
      (.fill))))

(defn clear-canvas [] (.clearRect ctx 0 0 1000 1000))
(defn init-canvas [] (set-canvas-size canvas 1000 1000))

; CANVAS END

(defn parse-event
  [event]
  (get {"ArrowUp"    :up
        "ArrowDown"  :down
        "ArrowLeft"  :left
        "ArrowRight" :right}
       (.-key event)))

(defn key-handler
  [chan event]
  (.preventDefault event)
  (put! chan (parse-event event)))

(defonce app-state (atom nil))
(def event-chan (chan))
(js/window.addEventListener "keydown" (partial key-handler event-chan))
(init-canvas)
(engine/start app-state event-chan)

(rum/defc Root < rum/reactive []
  [:div "Player: " (get-in (rum/react app-state) [:player :coordinates])])

;; Use requestAnimationFrame as callback?
(add-watch app-state :ticker (fn [_ _ _ state]
                               (clear-canvas)
                               (doseq [coordinate (s/get-coordinates state)] (draw-rect coordinate))
                               (doseq [food-coordinate (s/get-food state)] (draw-circle food-coordinate))))
(rum/mount (Root)
           (. js/document (getElementById "app")))
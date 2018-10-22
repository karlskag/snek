(ns snek.main
  (:require [rum.core :as rum]
            [snek.engine :as engine]
            [cljs.core.async :refer [chan put!]]))

(enable-console-print!)

; CANVAS BEGIN

(def default-sizes {:height 10 :width 10})
(def canvas (. js/document (getElementById "playArea")))
(def ctx (.getContext canvas "2d"))

(defn set-canvas-size
  [canvas width height]
  (set! (.-width canvas) width)
  (set! (.-height canvas) height))

(defn draw-rect
  [[x y]]
  (.fillRect ctx x y (:height default-sizes) (:width default-sizes)))

(defn clear-canvas [] (.clearRect ctx 0 0 500 500))
(defn init-canvas [] (set-canvas-size canvas 500 500))

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
                               (apply draw-rect (get-in state [:player :coordinates]))))
(rum/mount (Root)
           (. js/document (getElementById "app")))
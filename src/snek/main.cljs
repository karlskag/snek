(ns snek.main
  (:require [rum.core :as rum]
            [snek.engine :as engine]
            [cljs.core.async :refer [chan put!]]
            [snek.state :as s]))

(enable-console-print!)

; CANVAS BEGIN
(def colors {:snake "#000000" :food "#FF0000" :background "#EAC759"})
(def default-sizes {:entities  {:height 10 :width 10}
                    :play-area {:height 400 :width 800}})
(def canvas (. js/document (getElementById "playArea")))
(def ctx (.getContext canvas "2d"))

(defn set-canvas-size
  [canvas width height]
  (set! (.-width canvas) width)
  (set! (.-height canvas) height))

(defn draw-snake-rect
  [[x y]]
  (let [width  (get-in default-sizes [:entities :width])
        height (get-in default-sizes [:entities :height])]
    (set! (.-fillStyle ctx) (:snake colors))
    (.fillRect ctx
               (* x width)
               (* y height)
               height
               width)))

(defn draw-food-circle
  [[x y]]
  (let [width  (get-in default-sizes [:entities :width])
        height (get-in default-sizes [:entities :height])]
    (set! (.-fillStyle ctx) (:food colors))
    (doto ctx
      (.beginPath)
      (.arc (+ (* x width) (/ width 2)) (+ (* y height) (/ height 2)) 5 0 (* 2 js/Math.PI))
      (.fill))))

(defn paint-background [sizes]
  (set! (.-globalAlpha ctx) 0.65)
  (set! (.-fillStyle ctx) (:background colors))
  (.fillRect ctx 0 0 (:width sizes) (:height sizes))
  (set! (.-globalAlpha ctx) 1))

(defn clear-canvas [] (.clearRect ctx 0 0 1000 500))
(defn init-canvas [sizes] (set-canvas-size canvas (:width sizes) (:height sizes)))

; CANVAS END

(defn parse-keydown-event
  [event]
  (get {"ArrowUp"    :up
        "ArrowDown"  :down
        "ArrowLeft"  :left
        "ArrowRight" :right
        " "          :start-boost}
       (.-key event)))

(defn parse-keyup-event
  [event]
  (get {" " :end-boost}
       (.-key event)))

(defn keydown-handler
  [chan event]
  (.preventDefault event)
  (put! chan (parse-keydown-event event)))

(defn keyup-handler
  [chan event]
  (.preventDefault event)
  (if (= (.-key event) " ")
    (put! chan (parse-keyup-event event))))

(defonce app-state (atom nil))
(def event-chan (chan))
(js/window.addEventListener "keydown" (partial keydown-handler event-chan))
(js/window.addEventListener "keyup" (partial keyup-handler event-chan))
(init-canvas (:play-area default-sizes))
(engine/start app-state event-chan default-sizes)

(rum/defc Root < rum/reactive []
  [:div "Player-lenght: " (count (get-in (rum/react app-state) [:player :coordinates]))])

;; TODO: Send play area w/h to game for food placement and coordinate wrapping/wall crashing!
(add-watch app-state :ticker (fn [_ _ _ state]
                               (paint-background (:play-area default-sizes))
                               (doseq [coordinate (s/get-coordinates state)] (draw-snake-rect coordinate))
                               (doseq [food-coordinate (s/get-food state)] (draw-food-circle food-coordinate))))
(rum/mount (Root)
           (. js/document (getElementById "app")))
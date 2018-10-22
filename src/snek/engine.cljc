(ns snek.engine
  (:require
    [snek.core :as c]
    [cljs.core.async :as async]))

(def default-tick-speed 30)

(defn game-loop
  [state-atom channel]
  ; Needs change to avoid state update before tick-speed!
  ; Channel commands can set direction on state but not add movement,
  ; movement and co-ord update should happen only on set interval.
  (async/go-loop []
           (let [[command ch] (async/alts! [channel (async/timeout default-tick-speed)])]
             (cond
               (c/direction-event? command) (swap! state-atom c/handle-direction command)
               ;; pause event?
               ;; reset event?
               :else (swap! state-atom c/handle-tick)))
           (recur)))

(defn start
  [state-atom event-channel]
  (swap! state-atom c/initialize-game)
  (game-loop state-atom event-channel))
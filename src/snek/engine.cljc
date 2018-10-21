(ns snek.engine
  (:require
    [snek.core :as c]
    [cljs.core.async :as async]))

(def default-tick-speed 500)

(defn game-loop
  [state-atom channel]
  (async/go-loop []
           (let [[command ch] (async/alts! [channel (async/timeout default-tick-speed)])] ;take command and update state or tick
             (cond
               (c/movement-event? command) (swap! state-atom c/handle-movement command)
               :else (swap! state-atom c/handle-tick)))
           (recur)))

(defn start
  [state-atom event-channel]
  (swap! state-atom c/initialize-game)
  (game-loop state-atom event-channel))
(ns snek.engine
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [snek.core :as c]
            [cljs.core.async :as async :refer [chan put! alts! timeout <!]]))

(def default-tick-speed 30)

(defn game-loop
  [state-atom channel]
  (go (let [[command ch] (alts! [channel (timeout default-tick-speed)])] ;take command and update state or tick
        (cond
          (c/movement-event? command) (swap! state-atom (c/handle-movement command))
          :else (swap! state-atom (c/handle-tick))))))

(defn start
  [state-atom event-channel]
  (swap! state-atom core/initialize-game)
  (game-loop state-atom event-channel))
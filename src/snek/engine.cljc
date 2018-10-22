(ns snek.engine
  (:require
    [snek.core :as c]
    [cljs.core.async :as async]))

(def default-tick-speed 100)

(defn handle-ui-events
  [state-atom channel]
  (async/go-loop
    []
    (let [command (async/<! channel)]
      (cond
        (c/direction-event? command) (swap! state-atom c/handle-direction command)
        ;; pause event?
        ;; reset event?
        ))
    (recur)))

(defn game-loop
  [state-atom]
  (async/go-loop
    []
    (async/<! (async/timeout default-tick-speed))
    (swap! state-atom c/handle-tick)
    (recur)))

(defn start
  [state-atom event-channel]
  (swap! state-atom c/initialize-game)
  (handle-ui-events state-atom event-channel)
  (game-loop state-atom))
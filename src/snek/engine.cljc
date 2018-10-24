(ns snek.engine
  (:require
    [snek.core :as c]
    [cljs.core.async :as async]))

(def default-tick-speed 50)

(defn handle-ui-events
  [state-atom channel]
  (async/go-loop
    []
    (let [command (async/<! channel)]
      ;TODO: change to case
      (cond
        (c/direction-event? command) (swap! state-atom c/handle-direction command)
        (= :start-boost command) (def default-tick-speed 10)
        (= :end-boost command) (def default-tick-speed 50)
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
  [state-atom event-channel default-sizes]
  (swap! state-atom c/initialize-game default-sizes)
  (handle-ui-events state-atom event-channel)
  (game-loop state-atom))
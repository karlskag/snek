(ns snek.core
  (:require
    [clojure.test :refer [is]]
    [snek.state :as s]))

(defn movement-event?
  [evt]
  (contains? #{:up :down :left :right} evt))

(defn initialize-game
  []
  (s/create-default-state))

(defn move
  {:test (fn []
           (is (= (s/get-coordinates (move (s/create-default-state)))
                  [[9 3] [8 3] [7 3]]))
           (is (= (s/get-coordinates (move {:player {:coordinates [[8 3] [7 3] [6 3]]
                                                     :movements   [[1 0] [1 0] [1 0] [0 -1]]}
                                            :food   [[20 6]]}))
                  [[8 2] [8 3] [7 3]])))}
  [state]
  ;; Head moves first, then body makes same movement delayed one tick.
  ;; Each tick adds a movement co-ord to list on state.
  ;; Each part of snake makes corresponding index move
  (s/update-coordinates state (vec (map-indexed (fn [index coordinate]
                                                  (map + coordinate (nth (s/get-movements state)
                                                                         (dec (- (count (s/get-movements state)) index)))))
                                                (s/get-coordinates state)))))

(defn handle-movement
  {:test (fn []
           (is (= (handle-movement (s/create-default-state) :down)
                  {:player {:coordinates [[8 2] [8 3] [7 3]]
                            :movements   [[1 0] [1 0] [1 0] [0 -1]]}
                   :food   [[20 6]]})))}
  [state command]
  (-> (case command
        :up (s/update-movements state [0 1])
        :down (s/update-movements state [0 -1])
        :left (s/update-movements state [-1 0])
        :right (s/update-movements state [1 0]))
      (move)))

(defn handle-tick
  {:test (fn []
           (is (= (handle-tick (s/create-default-state))
                  {:player {:coordinates [[9 3] [8 3] [7 3]]
                            :movements   [[1 0] [1 0] [1 0] [1 0]]}
                   :food   [[20 6]]})))}
  [state]
  (-> state
      (s/update-movements (last (s/get-movements state)))
      (move)))

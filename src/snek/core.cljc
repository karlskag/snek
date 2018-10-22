(ns snek.core
  (:require
    [clojure.test :refer [is]]
    [snek.state :as s]))

(defn direction-event?
  [evt]
  (contains? #{:up :down :left :right} evt))

(defn initialize-game
  []
  (s/create-default-state))

(defn calculate-coordinates
  [state]
  (vec (map-indexed (fn [index coordinate]
                      (map + coordinate (nth (s/get-movements state)
                                             (dec (- (count (s/get-movements state)) index)))))
                    (s/get-coordinates state))))

(defn grow-with
  {:test (fn []
           (is (= (grow-with {:player {:coordinates [[1 1] [2 2]]}} [3 3])
                  {:player {:coordinates [[1 1] [2 2] [3 3]]}})))}
  [state coordinate]
  (s/add-coordinate state coordinate))

(defn should-grow?
  {:test (fn []
           (is (= (should-grow? {:player {:coordinates [[1 1] [2 1]]}
                                 :food   #{[1 1]}})
                  true)))}
  [state]
  (let [head (first (s/get-coordinates state))]
    (contains? (s/get-food state) head)))

(defn should-crash?
  [state]
  (let [head (first (s/get-coordinates state))
        body (drop 1 (s/get-coordinates state))]
    (contains? body head)))

(defn move
  {:test (fn []
           (is (= (s/get-coordinates (move (s/create-default-state)))
                  [[9 3] [8 3] [7 3]]))
           (is (= (s/get-coordinates (move {:player {:coordinates [[8 3] [7 3] [6 3]]
                                                     :movements   [[1 0] [1 0] [1 0] [0 -1]]}
                                            :food   #{[20 6]}}))
                  [[8 2] [8 3] [7 3]])))}
  [state]
  ;; Head moves first, then body makes same movement delayed one tick.
  ;; Each tick adds a movement co-ord to list on state.
  ;; Each part of snake makes corresponding index move
  (if (should-grow? state)
    (let [tail (last (s/get-coordinates state))]
      (-> state
          (s/update-coordinates (calculate-coordinates state))
          (grow-with tail)))
    (s/update-coordinates state (calculate-coordinates state))))

(defn handle-direction
  [state direction]
  (s/update-direction state direction))

(defn handle-movement
  {:test (fn []
           (is (= (handle-movement (s/update-direction (s/create-default-state) :down))
                  {:player {:direction   :down
                            :coordinates [[8 2] [8 3] [7 3]]
                            :movements   [[1 0] [1 0] [1 0] [0 -1]]}
                   :food   #{[20 6]}})))}
  [state]
  (case (s/get-direction state)
    :up (s/update-movements state [0 -1])
    :down (s/update-movements state [0 1])
    :left (s/update-movements state [-1 0])
    :right (s/update-movements state [1 0]))
  )

(defn handle-tick
  {:test (fn []
           (is (= (handle-tick (s/create-default-state))
                  {:player {:coordinates [[9 3] [8 3] [7 3]]
                            :movements   [[1 0] [1 0] [1 0] [1 0]]}
                   :food   [[20 6]]})))}
  [state]
  (-> state
      (handle-movement)
      (move)))

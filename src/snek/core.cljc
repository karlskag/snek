(ns snek.core
  (:require
    [clojure.test :refer [is]]
    [snek.state :as s]))

(defn direction-event?
  [evt]
  (contains? #{:up :down :left :right} evt))

(defn initialize-game
  [_ sizes]
  (s/create-default-state sizes))

(defn calculate-coordinates
  {:test (fn []
           (is (= (calculate-coordinates {:sizes  {:play-area {:height 10 :width 20}
                                                   :entities  {:height 1 :width 1}}
                                          :player {:movements   [[1 0] [1 0] [0 -1]]
                                                   :coordinates [[3 0] [2 0] [1 0]]}})
                  [[3 9] [3 0] [2 0]])))}
  [state]
  (vec (map-indexed (fn [index coordinate]
                      (let [[new-x new-y] (map + coordinate (nth (s/get-movements state)
                                                                 (dec (- (count (s/get-movements state)) index))))]
                        (cond
                          ;to the left of play area
                          (< new-x 0) [(+ (s/get-transformed-play-area-width state) new-x) new-y]
                          ;to the right of play area
                          (> new-x (- (s/get-transformed-play-area-width state) 1)) [(mod new-x (s/get-transformed-play-area-width state)) new-y]
                          ;above play area
                          (< new-y 0) [new-x (+ (s/get-transformed-play-area-height state) new-y)]
                          ;below play area
                          (> new-y (- (s/get-transformed-play-area-height state) 1)) [new-x (mod new-y (s/get-transformed-play-area-height state))]
                          :else [new-x new-y])))
                    (s/get-coordinates state))))

(defn grow-with
  {:test (fn []
           (is (= (grow-with {:player {:coordinates [[1 1] [2 2]]}} [3 3])
                  {:player {:coordinates [[1 1] [2 2] [3 3]]}})))}
  [state coordinate]
  (s/add-coordinate state coordinate))

(defn should-grow?
  {:test (fn []
           (is (should-grow? {:player {:coordinates [[1 1] [2 1]]}
                              :food   #{[1 1]}})))}
  [state]
  (let [head (first (s/get-coordinates state))]
    (contains? (s/get-food state) head)))

(defn crash?
  [state]
  (let [head (first (s/get-coordinates state))
        body (drop 1 (s/get-coordinates state))]
    (some (partial = head) body)))

(defn eat-food
  [state]
  (s/remove-food state (first (s/get-coordinates state))))

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
          (eat-food)
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

(defn maybe-add-food
  [state]
  (if (or (< (rand-int 1000) 10) (empty? (s/get-food state)))
    (s/update-food state [(rand-int (s/get-transformed-play-area-width state)) (rand-int (s/get-transformed-play-area-height state))])
    state))

(defn handle-tick
  {:test (fn []
           (is (= (handle-tick (s/create-default-state))
                  {:player {:coordinates [[9 3] [8 3] [7 3]]
                            :movements   [[1 0] [1 0] [1 0] [1 0]]}
                   :food   [[20 6]]})))}
  [state]
  (if (crash? state)
    state
    (-> state
        ; TODO: rename handle movement or put inside move fn
        (handle-movement)
        (move)
        (maybe-add-food))))

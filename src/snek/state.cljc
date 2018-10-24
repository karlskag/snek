(ns snek.state
  (:require [clojure.test :refer [is]]))

(defn create-default-state
  ([]
   ({:player {:direction   :right
              :coordinates [[8 3] [7 3] [6 3]]
              :movements   [[1 0] [1 0] [1 0]]}
     :food   #{[5 6] [3 5]}
     :sizes  {:play-area {:width 400 :height 200}
              :entities {:width 10 :height 10}}}))
  ([sizes]
  {:player {:direction   :right
            :coordinates [[8 3] [7 3] [6 3]]
            :movements   [[1 0] [1 0] [1 0]]}
   :food   #{[5 6] [3 5]}                                   ;TODO: Randomly generate positions of snake and food given play-area sizes
   :sizes sizes}))

(defn get-movements
  [state]
  (get-in state [:player :movements]))

(defn update-movements
  {:test (fn []
           (is (= (get-movements (update-movements (create-default-state) [0 1]))
                  [[1 0] [1 0] [1 0] [0 1]])))}
  [state movement]
  (update-in state [:player :movements] conj movement))

(defn get-coordinates
  [state]
  (get-in state [:player :coordinates]))

(defn update-coordinates
  {:test (fn []
           (is (= (get-coordinates (update-coordinates (create-default-state) [[0 1] [1 1]]))
                  [[0 1] [1 1]])))}
  [state coordinate]
  (assoc-in state [:player :coordinates] coordinate))

(defn get-direction
  [state]
  (get-in state [:player :direction]))

(defn update-direction
  [state direction]
  (assoc-in state [:player :direction] direction))

(defn get-food
  [state]
  (:food state))

(defn add-coordinate
  [state coordinate]
  (update-in state [:player :coordinates] conj coordinate))

(defn remove-food
  [state coordinate]
  (update state :food disj coordinate))

(defn update-food
  [state coordinate]
  (update state :food conj coordinate))

(defn get-transformed-play-area-width
  {:test (fn []
           (is (= (get-transformed-play-area-width {:sizes {:play-area {:width 500}
                                                            :entities {:width 10}}}) 50)))}
  [state]
  (/ (get-in state [:sizes :play-area :width]) (get-in state [:sizes :entities :width])))

(defn get-transformed-play-area-height
  [state]
  (/ (get-in state [:sizes :play-area :height]) (get-in state [:sizes :entities :height])))


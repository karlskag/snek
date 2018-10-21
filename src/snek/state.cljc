(ns snek.state
  (:require [clojure.test :refer [is]]))

(defn create-default-state
  []
  {:player {:coordinates [[8 3] [7 3] [6 3]]
            :movements   [[1 0] [1 0] [1 0]]}
   :food   [[20 6]]})

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
  [state coordinates]
  (assoc-in state [:player :coordinates] coordinates))


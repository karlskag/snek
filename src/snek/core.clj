(ns snek.core
  (:require
    [clojure.test :refer [is]]
    [clojure.string :refer [join]]
    [snek.state :as s]))

(def signs {:food "@"
            :head "o"
            :body "x"})

(defn movement-event?
  [evt]
  (contains? #{:up :down :left :right} evt))

(defn move
  {:test (fn []
           (is (= (s/get-coordinates (move (s/create-default-state)))
                  [[9 3] [8 3] [7 3]]))
           (is (= (s/get-coordinates (move {:player {:coordinates [[8 3] [7 3] [6 3]]
                                                     :movements   [[1 0] [1 0] [1 0] [-1 0]]}
                                            :food   [[20 6]]}))
                  [[8 2] [8 3] [7 3]])))}
  [state]
  ;; Head moves first, then body makes same movement delayed one tick.
  ;; Each tick adds a movement co-ord to list on state.
  ;; Each part of snake makes corresponding index move
  (s/update-coordinates state (reverse (vec (map-indexed (fn [index coordinate]
                                                           (map + coordinate (nth (s/get-movements state) index)))
                                                         (reverse (s/get-coordinates state)))))))

(defn handle-movement
  {:test (fn []
           (is (= (handle-movement (s/create-default-state) :down)
                  {:player {:coordinates [[8 2] [8 3] [7 3]]
                            :movements   [[1 0] [1 0] [1 0] [-1 0]]}
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

(defn create-play-area
  {:test (fn []
           (is (= (create-play-area {:height 2 :width 3}) [["-" "-" "-"]
                                                           ["-" "-" "-"]])))}
  [size]
  (vec (repeatedly (:height size) #(vec (map (fn [_] "-") (range (:width size)))))))

(defn insert-sign-on-coordinates
  {:test (fn []
           (is (= (insert-sign-on-coordinates [1 2] (:food signs) (create-play-area {:height 3 :width 2}))
                  [["-" "-"]
                   ["-" "-"]
                   ["-" "@"]])))}
  [coordinates sign area]
  (let [x-coord (first coordinates)
        y-coord (second coordinates)]
    (assoc-in area [y-coord x-coord] sign)))

(defn insert-sign-on-multiple-coordinates
  [coordinates-coll sign area]
  (reduce (fn [matrix coordinate]
            (insert-sign-on-coordinates coordinate sign matrix))
          area
          coordinates-coll))

(defn place-food
  {:test (fn []
           (is (= (place-food {:food [[1 1]]} (create-play-area {:height 3 :width 3}))
                  [["-" "-" "-"]
                   ["-" "@" "-"]
                   ["-" "-" "-"]]))
           (is (= (place-food {:food [[1 1] [2 2]]} (create-play-area {:height 3 :width 3}))
                  [["-" "-" "-"]
                   ["-" "@" "-"]
                   ["-" "-" "@"]])))}
  [state play-area]
  (insert-sign-on-multiple-coordinates (:food state) (:food signs) play-area))

(defn place-snek
  {:test (fn []
           (is (= (place-snek {:player {:direction   :R
                                        :coordinates [[2 1] [1 1] [1 2]]}}
                              (create-play-area {:height 3 :width 3}))
                  [["-" "-" "-"]
                   ["-" "x" "o"]
                   ["-" "x" "-"]])))}
  [state play-area]
  (let [head (first (get-in state [:player :coordinates]))
        body (drop 1 (get-in state [:player :coordinates]))]
    (->> play-area
         (insert-sign-on-coordinates head (:head signs))
         (insert-sign-on-multiple-coordinates body (:body signs)))))

(defn populate-play-area
  {:test (fn []
           (is (= (populate-play-area
                    {:player {:direction   :R
                              :coordinates [[2 1] [1 1]]}
                     :food   [[0 0]]}
                    (create-play-area {:height 3 :width 3}))
                  [["@" "-" "-"]
                   ["-" "x" "o"]
                   ["-" "-" "-"]])))}
  [state play-area]
  (->> play-area
       (place-food state)
       (place-snek state)))

(defn state->string
  {:test (fn []
           (is (= (state->string (s/create-default-state)) ["------------------------------"
                                                            "------------------------------"
                                                            "------------------------------"
                                                            "------xxo---------------------"
                                                            "------------------------------"
                                                            "------------------------------"
                                                            "--------------------@---------"
                                                            "------------------------------"
                                                            "------------------------------"
                                                            "------------------------------"])))}
  [state]
  (->> (create-play-area {:height 10 :width 30})
       (populate-play-area state)
       (map join)
       (flatten)))

(comment
  (doseq [item (state->string (s/create-default-state))]
    (println item))
  )

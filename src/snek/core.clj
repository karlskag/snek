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
           ())}
  [state]
  ;; Head moves first, then body makes same movement delayed one tick.
  ;; Each tick adds a movement co-ord to list on state.
  ;; Each part of snake makes corresponding index move
  (s/update-coordinates state (map-indexed (fn [coordinate index]
                                             (map + coordinate (nth (s/get-movements state) index)))
                                           (s/get-coordinates state)))

  (defn handle-movement
    [state command]
    (case command
      :up (s/update-movements state [1 0])
      :down (s/update-movements state [-1 0])
      :left (s/update-movements state [0 -1])
      :right (s/update-movements state [1 0]))
    (move state))

  (defn handle-tick
    [state]
    (s/update-movements state (last (s/get-movements state))))

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

(ns snek.state)

(defn create-default-state
  []
  {:player {:direction   :R
            :coordinates [[8 3] [7 3] [6 3]]
            :movements [[1 0] [1 0] [1 0]]}
   :food   [[20 6]]})


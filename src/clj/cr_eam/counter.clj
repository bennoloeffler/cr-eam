(ns cr-eam.counter)

(def counter (atom 0N))
(def last-time (atom (System/currentTimeMillis)))


(defn duration []
  (let [this-time (System/currentTimeMillis)
        duration  (as-> @last-time $
                        (- this-time $)
                        (long $))
        result    (str duration " msecs")]
    (reset! last-time this-time)
    result))


(defn inc-counter []
  (swap! counter inc)
  ;;(println @counter)
  (let [timing (if (== 0 (mod @counter 100))
                 (duration)
                 "")
        _      (when (not= timing "") (println "t: " timing " msecs for 100 requests"))
        body   (str (format "counter: %,d" (biginteger @counter)) " " timing)]

    body))

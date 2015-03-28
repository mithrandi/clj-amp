(ns clj-amp.bigbytes
  (:require [clj-amp.argument :as a]
            [byte-streams :as bs]
            [plumbing.core :refer [for-map]]))

;; Also known as "BigString"
(defmethod a/from-box ::big-bytes
  [argument box]
  (for [idx (range)
        :let [key
              (str
               (:name argument)
               (if (= idx 0) "" (str "." (+ idx 1))))]
        :while (contains? box key)]
    (get box key)))


(defmethod a/to-box ::big-bytes
  [argument value]
  (let [chunk-size (get argument :chunk-size 0xffff)]
    (into
     {}
     (map-indexed
      (fn [idx buf]
        [(str
          (:name argument)
          (if (= idx 0) "" (str "." (+ idx 1))))
         buf])
      (bs/convert value
                  (bs/seq-of java.nio.ByteBuffer)
                  {:chunk-size chunk-size})))))

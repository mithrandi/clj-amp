(ns clj-amp.box
  (:require [gloss.core :refer
             [defcodec header string finite-block byte-count compile-frame nil-frame]]
            [gloss.core.codecs :refer [wrap-suffixed-codec]]
            [gloss.io]
            [manifold.stream :as s]
            [manifold.deferred :as d]
            [slingshot.slingshot :refer [throw+]]
            [plumbing.core :refer [map-vals]]))


(def max-key-length 0xff)


(def max-value-length 0xffff)


(defn validate-box
  "Validate the following invariants of an AMP box:
    - No key is 0 bytes long.
    - No key is longer than 0xff bytes.
    - No value is longer than 0xffff bytes."
  [box]
  (doseq [[key value] box]
    (if (empty? key)
      (throw+ {:type ::empty-key}))
    (if (> (count key) max-key-length)
      (throw+ {:type ::key-too-long :key key}))
    (if (> (byte-count value) max-value-length)
      (throw+ {:type ::value-too-long :key key})))
  box)


(defcodec ampbox-item-codec
  (header
   :uint16-be
   (fn [len]
     (if (zero? len)
       nil-frame
       (compile-frame
        [(string "iso-8859-1" :length len)
         (finite-block :uint16-be)])))
   (fn [tuple]
     (if (nil? tuple)
       0
       (count (first tuple))))))


(defn wrap-duplex-stream
  [protocol s]
  (let [out (s/stream)]
    (s/connect
     (s/map (partial gloss.io/encode protocol) out)
      s)
    (s/splice
      out
      (gloss.io/decode-stream s protocol))))


(defn wrap-ampbox-stream
  [s]
  (let [s'  (wrap-duplex-stream ampbox-item-codec s)
        out (s/stream)]
    (s/connect
     (s/mapcat #(conj (vec %) nil) out)
     s')
    (s/splice
     out
     (s/transform
      (comp (partition-by nil?) 
            (take-nth 2)
            (map (partial into {})))
      s'))))


(defn on- [op f x y] (op (f x) (f y)))


(defn boxes=
  "Compare two boxes for equality"
  [box1 box2]
  (on- = (partial map-vals gloss.io/to-byte-buffer) box1 box2))

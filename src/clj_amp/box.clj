(ns clj-amp.encoding
  (:require [gloss.core :refer
             [defcodec repeated compile-frame string finite-block byte-count]]
            [gloss.core.codecs :refer [wrap-suffixed-codec]]
            [slingshot.slingshot :refer [throw+]]))


(def max-key-length 0xff)


(def max-value-length 0xffff)


(defcodec ampbox-key
  (string "iso-8859-1" :prefix :uint16-be))


(defcodec ampbox-value
  (finite-block :uint16-be))


(def ampbox-codec
  (compile-frame
   (wrap-suffixed-codec
    "\0\0"
    (compile-frame
     (repeated
      [ampbox-key ampbox-value]
      :prefix :none)))
   vec
   (partial into {})))


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

(ns clj-amp.encoding
  (:require [gloss.core :refer
             [defcodec repeated compile-frame string finite-block]]
            [gloss.core.codecs :refer [wrap-suffixed-codec]]))


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
   (partial into [])
   (partial into {})))
                   

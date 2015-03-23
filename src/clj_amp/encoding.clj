(ns clj-amp.encoding
  (:require [gloss.core :refer [defcodec delimited-frame repeated
                                ordered-map compile-frame string]]
            [gloss.core.codecs :refer [wrap-suffixed-codec]]))

(defcodec ampbox-codec
  (wrap-suffixed-codec
   [0 0]
   (compile-frame
    (repeated
     (ordered-map :key (string "iso-8859-1" :prefix :uint16-be)
                  :value (repeated :byte :prefix :uint16-be))
     :prefix :none))))
                   

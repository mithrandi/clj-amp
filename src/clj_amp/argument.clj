(ns clj-amp.argument
  (:require [gloss.core :as g]
            [gloss.io]
            [clj-time.format :as f]))


(defmulti to-box
  "Serialize a Command argument to a map of Box keys/values."
  (fn [argument value] (:type argument)))


(defmethod to-box ::bytes
  [argument value]
  {(:name argument) value})


(defmulti from-box
  "Deserialize a Command argument from a Box."
  (fn [argument box] (:type argument)))


(defmethod from-box ::bytes
  [argument box]
  (get box (:name argument)))


(defn defargument
  "Define an AMP argument type using a gloss codec."
  [type codec]
  (defmethod to-box type
     [argument value]
     {(:name argument) (gloss.io/encode codec value)})
  (defmethod from-box type
     [argument box]
     (gloss.io/decode codec (get box (:name argument)))))


;; TODO: Make this use a BigNum if necessary
(defargument ::integer
  (g/compile-frame (g/string-integer :ascii)))


(defargument ::string
  (g/compile-frame (g/string :utf-8)))


(defargument ::boolean
  (g/compile-frame
   (g/string :ascii)
   #(if %1 "True" "False")
   (partial = "True")))


;; This is represented as the Clojure double type
(defargument ::float
  (g/compile-frame (g/string-float :ascii)))


(defargument ::decimal
  (g/compile-frame
   (g/string :ascii)
   str
   bigdec))


(def amp-time-formatter
  (f/formatter "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZ"))


(defargument ::date-time
  (g/compile-frame
   (g/string :ascii)
   (partial f/unparse amp-time-formatter)
   (partial f/parse amp-time-formatter)))

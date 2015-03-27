(ns clj-amp.argument
  (:require [gloss.core :as g]
            [gloss.io]
            [clj-time.format :as f]
            [slingshot.slingshot :refer [throw+]]))


(defn argument-or-type
  [a] (if (map? a) a {:type a}))


(defmulti from-box
  "Deserialize a Command argument from a Box."
  (fn [argument box] (:type argument)))


(defmulti to-box
  "Serialize a Command argument to a map of Box keys/values."
  (fn [argument value] (:type argument)))


(defmulti from-bytes
  "Deserialize an argument from a single value."
  (fn [argument value] (:type argument)))


(defmulti to-bytes
  "Serialize an argument as a single value"
  (fn [argument value] (:type argument)))


(defmethod from-box :default
  [argument box]
  (let [name (:name argument)]
    (if (contains? box name)
      (from-bytes argument (get box name))
      (when-not (:optional? argument)
        (throw+ {:type ::missing-argument :name name})))))


(defmethod to-box :default
  [argument value]
  {(:name argument) (to-bytes argument value)})


(defmethod from-bytes ::bytes
  [argument value]
  value)


(defmethod to-bytes ::bytes
  [argument value]
  value)


(defn defargument
  "Define an AMP argument type using a gloss codec."
  [type codec]
  (defmethod to-bytes type
     [argument value]
     (gloss.io/encode codec value))
  (defmethod from-bytes type
     [argument value]
     (gloss.io/decode codec value)))


;; TODO: Make this use a BigNum if necessary
(defargument ::integer
  (g/compile-frame (g/string-integer :ascii)))


(defargument ::string
  (g/compile-frame (g/string :utf-8)))


(defargument ::byte-string
  (g/compile-frame (g/string :iso-8859-1)))


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


(g/defcodec- amp-list
  (g/compile-frame
   (g/repeated (g/finite-block :uint16-be) :prefix :none)))


;; Corresponds to ListOf() in the Python implementation
(defmethod from-bytes ::list
  [argument value]
  (let [of (argument-or-type (:of argument))]
    (->> value
         (gloss.io/decode amp-list)
         (map (partial from-bytes of)))))


(defmethod to-bytes ::list
  [argument value]
  (let [of (argument-or-type (:of argument))]
    (->> value
         (map (partial to-bytes of))
         (gloss.io/encode amp-list))))


;; Corresponds to AmpList() in the Python implementation, which has a terribly
;; confusing name.

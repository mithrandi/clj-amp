(ns clj-amp.command
  (:require [clj-amp.argument :as a]
            [plumbing.core :refer [for-map]]
            [slingshot.slingshot :refer [throw+]]))


(defmacro defcommand
  "Define an AMP Command.
  Usage: (defcommand
          name
          command-name?
          {argument-name {:type argument-type
                          :name amp-argument-name?
                          ...}
           ...}
           return-value)"
  ([defname arguments return-value]
   `(def ~defname (build-command ~(name defname) ~arguments ~return-value)))
  ([defname command-name arguments return-value]
   `(def ~defname (build-command ~command-name ~arguments ~return-value))))


(defn build-command
  "Build an AMP Command."
  [command-name arguments return-value]
  {:name command-name
   :arguments arguments
   :response return-value})


(defn from-box
  [command box]
  (for-map [[name argument] (:arguments command)
            :let [value (a/from-box argument box)]
            :when (not (nil? value))]
           name
           (a/from-box argument box)))


(defn to-box
  [command values]
  (apply
   merge
   (for [[name argument] (:arguments command)]
     (let [value (get values name)]
       (if (nil? value)
         (if (:optional? argument)
           {}
           (throw+ {:type ::missing-argument :name name}))
         (a/to-box argument value))))))


(ns clj-amp.command
  (:require [clj-amp.argument :as a]
            [plumbing.core :refer [for-map]]
            [slingshot.slingshot :refer [throw+]]))


(defmacro defcommand
  "Define an AMP Command.
  Usage: (defcommand
          name
          {argument-name {:type argument-type
                          :name amp-argument-name?
                          ...}
           ...}
          {response-name {:type argument-type
                          :name amp-response-name?
                          ...}
          :command-name command-name?)"
  [defname arguments response & {:keys [command-name]
                                 :or {command-name (name defname)}}]
  `(def ~defname
     (build-command ~command-name ~arguments ~response)))


(defn build-command
  "Build an AMP Command."
  [command-name arguments response]
  {:name command-name
   :arguments
   (for-map [[argument-name argument] arguments]
            argument-name
            (assoc argument
                   :name
                   (get argument :name (name argument-name))))
   :response response})


(defn from-box
  [arguments box]
  (for-map [[name argument] arguments
            :let [value (a/from-box argument box)]
            :when (not (nil? value))]
           name
           (a/from-box argument box)))


(defn to-box
  [arguments values]
  (apply
   merge
   (for [[name argument] arguments]
     (let [value (get values name)]
       (if (nil? value)
         (if (:optional? argument)
           {}
           (throw+ {:type ::missing-argument :name name}))
         (a/to-box argument value))))))


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
  [defname arguments response & {:keys [command-name errors]
                                 :or {command-name (name defname)
                                      errors       {}}}]
  `(def ~defname
     (build-command ~command-name ~arguments ~response ~errors)))


(defn- name-defaults
  [arguments]
  (for-map [[argument-name argument] arguments]
            argument-name
            (assoc argument
                   :name
                   (get argument :name (name argument-name)))))


(defn build-command
  "Build an AMP Command."
  [command-name arguments response errors]
  {:name       command-name
   :arguments  (name-defaults arguments)
   :response   (name-defaults response)
   :errors     errors
   :inv-errors (clojure.set/map-invert errors)})


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


(defcommand error
  {:code {:name "_error_code"
          :type ::a/byte-string}
   :desc {:name "_error_description"
          :type ::a/string}}
  {})


(def unknown-error-code "UNKNOWN")


(defn error-from-box
  [command box]
  (let [{:keys [code desc]
         :or {code unknown-error-code
              desc "No description"}}
        (from-box (:arguments error) box)
        error-type
        (get (:inv-errors command) code ::unknown-error)]
    (ex-info "Remote AMP error" {:type error-type :description desc})))


(defn error-to-box
  [command error]
  (let [error-type (:type (ex-data error))
        error-code (get (:errors command) error-type unknown-error-code)
        error-desc (pr-str error)]
    (to-box
     (:arguments error)
     {"_error_code"        error-code
      "_error_description" error-desc})))


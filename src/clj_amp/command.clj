(ns clj-amp.command)

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
  {:name command-name :arguments arguments :return-value return-value})



;; # Simple configuration framework

(ns confjure.core
  "Library to let the server developer manage their configuration data.

   There are several basic ideas here:
     * env-mode: The environment of the program to run. Built-in ::test
                 and ::production mode, and the default is ::test mode.
     * conf-map: Given an env-mode, there is just one effictive conf-map,
                 which is a map of conf-value
     * conf-value: k => v pair, where k must always be a keywork,
                   and the value can be any type.  
     * conf-elements: Configurable elements of program, work as a dictionary.
                      All configure values must just a instance of an element.
                      When you specify a conf-element, you must provide a
                      check function, the library will use it to check if a
                      conf-value is legal.")

;;--------------------------------------------------------------
;; # Immutable part

(defn value-legal?
  "check in a conf-map if value v is a legal config element of k.
   Three return value possible: :legal, :illegal or nil (not found)"
  [conf-map k v]
  {:pre [(map? conf-map) (keyword? k)]}
  (when-let [elem (get conf-map k)]
    (when-let [pred (:pred elem)]
      (if (pred v) :legal :illegal))))

(defn legality
  "Check if a val conform a pred.
   Three return value possible: :legal, :illegal or nil (val or pred is nil)"
  [val pred]
  (when (and (not (nil? val)) pred)
    (if (pred val) :legal :illegal)))

(defn check-values
  "Check if config values conform predefined conf-elements.
   Returns a map: conf-name => checking result."
  [conf-values conf-elements]
  {:pre [(map? conf-values) (map? conf-elements)]}
  (into {}
        (for [[conf-name {pred :pred}] conf-elements
              :let [val (conf-values conf-name)]]
          [conf-name (legality val pred)])))

(defn mk-error-message
  "Construct a error message for given conf-key and it's checking
   rslt (return by legality check) and the document of the conf-key."
  [rslt conf-key document]
  {:pre [(keyword? conf-key)]}
  (let [rslt (or rslt :nonexist)]
    (str (name rslt) " " (name conf-key) ": " document)))

(defn translate-results
  "translate the rslts of checking into error messages against
   config elements"
  [conf-elements rslts]
  {:pre [(map? rslts) (map? conf-elements)]}
  (for [[conf-name rslt] rslts
        :when (not= rslt :legal)
        :let [document (-> conf-elements conf-name :doc)
              msg (mk-error-message rslt conf-name document)]]
    msg))

(defn provide-value
  "Provide a configuration value of env to conf-values,
   giving current conf-elements, with config name k, and value v,
   returns updated conf-values or nil if k, v not conform conf-elements"
  [conf-values conf-elements env k v]
  {:pre [(map? conf-elements) (map? conf-values) (keyword? env) (keyword? k)]}
  (when (value-legal? conf-elements k v)
    (update-in conf-values [env] assoc k v)))

;; TODO mutable part to provide a central storage of all
;; configurations

(comment
  ;; When you want to introduce a new configuable element for your
  ;; program:
  (def-config :myconf
    "My sample config, must be one of :bar :baz"
    (fn [elem] (#{:foo :bar :baz} elem)))
  
  ;; You can provide values to config elements just for testing
  (provide-config [:c/test] {:myconf :foo})

  ;; You can provide different values for production
  (provide-config [:c/production] {:myconf :baz})

  ;; To specify which running environment the program is in,
  ;; you may add "-Drunning.environment=production" to your command
  ;; line, the default running environment is test

  ;; To check if all config elements have been provided a legal value,
  ;; use this when you start your program (e.g. in main)
  (check-config)
  ;; if some of the elements do not have meaningful value,
  ;; check-config will raise a error.
  ;; If succeed, it will go on, and output all effective config value
  ;; into a file, you can check the file.

  ;; To return a config value
  (load-config :myconf)

  ;; To change a config value in runtime
  (alter-config! :myconf :baz)
  )

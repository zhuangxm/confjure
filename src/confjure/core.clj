(ns confjure.core
  "A simple library to config an application")

;; -------------------------------------------------------
;; ## Immutable part
(defn- add-dict-item
  "Add new config element k with validator to conf-dict,
   option :strict with true, k can not be redefined"
  [conf-dict k validator & {:keys [strict] :or {strict false}}]
  (if (and strict (contains? conf-dict k))
    (throw (RuntimeException.
            (str k " has been introduced to the dictionary.")))
    (assoc conf-dict k validator)))

(defn- add-conf-value
  [conf-values new-conf]
  (merge conf-values new-conf))

(defn- conf-errors
  [conf-dict conf-values]
  (let [value-errors (for [[k validator] conf-dict
                           :let [val (get conf-values k)]
                           :when (and validator (not (validator val)))]
                       [k val])
        orphan-values (for [[k val] conf-values
                            :when (not (contains? conf-dict k))]
                        [k :orphan-value])]
    (seq (concat value-errors orphan-values))))

;;---------------------------------------------------------
;; ## Some in-memory store: 
;; * The config dictionary.
;;   The library will check the dictionary to make sure everything's
;;   configured properly.
(defonce ^:private the-dict (atom {}))

;; * The config value store.
(defonce ^:private the-values (atom {}))

;; * Flag to cache checked
(defonce ^:private checked (atom false))

;; Whenever the dict on values changed, we need to check
;; all the values again.
(doseq [obj [the-dict the-values]]
  (add-watch obj :fresh
             (fn [_ _ _ _] (swap! checked (fn [_] false)))))

(defn- env-mode
  []
  (keyword (or (System/getProperty "config.env") "test")))
;;----------------------------------------------------------
;; ## Interface functions
(defn introduce!
  "Introduce a new config into the dictionary.
   You may optional provide a validator to check if a value
   is validate for this config."
  ([k]
     (introduce! k nil))
  ([k validator]
   {:pre [(keyword? k)]}
   (swap! the-dict add-dict-item k validator
          :strict (not= :test (env-mode)))))

(defn provide!
  "Provide a config value v for config k, given in a config
   environment env. e.g. :test/:production.
   The application will check the system property config.env,
   only when env matches this property will be effective."
  [env conf-map]
  {:pre [(keyword? env) (map? conf-map)]}
  (when (= (env-mode) env)
    (swap! the-values add-conf-value conf-map)))

;; I think better to check the config implicitly when the
;; user try to load first config element.
(defn check-all
  "Check the config dictionary if all the config elements
   are set properly."
  []
  (when (not @checked)
    (swap! checked not)
    (when-let [errs (conf-errors @the-dict @the-values)]
      (throw (RuntimeException. (str errs))))))

(defn value
  "Read a config value of k.
   If k does not exist in dictionary, it will throw an exception."
  [k]
  {:pre [(keyword? k)]}
  (check-all)
  (if (contains? @the-dict k)
    (get @the-values k)
    (throw (RuntimeException. (str k " does not exist in dictionary")))))

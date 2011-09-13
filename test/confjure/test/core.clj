(ns confjure.test.core
  (:use [clojure.test]
        [confjure.core :reload-all true]))

(deftest test-conf-errors
  (let [conf-errors @#'confjure.core/conf-errors
        dict {:foo even? :bar fn? :baz nil}]
    (are [vals expected] (= (conf-errors dict vals) expected)
         {:foo 4 :bar identity} nil
         {:foo 3 :bar false} [[:foo 3] [:bar false]]
         {:foo 4 :non 5} [[:bar nil] [:non :orphan-value]])))

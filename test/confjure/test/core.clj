(ns confjure.test.core
  (:use [clojure.test]
        [confjure.core :reload-all true]))

(deftest test-add-dict-item
  (let [add-dict-item @#'confjure.core/add-dict-item]
    (is (thrown? RuntimeException (add-dict-item {:foo even?}
                                                 :foo odd? :strict true)))
    (is (= {:foo odd?} (add-dict-item {:foo even?} :foo odd? :strict false)))
    (is (= (add-dict-item {:foo even?} :bar nil?) {:foo even? :bar nil?}))))

(deftest test-conf-errors
  (let [conf-errors @#'confjure.core/conf-errors
        dict {:foo even? :bar fn? :baz nil}]
    (are [vals expected] (= (conf-errors dict vals) expected)
         {:foo 4 :bar identity} nil
         {:foo 3 :bar false} [[:foo 3] [:bar false]]
         {:foo 4 :non 5} [[:bar nil] [:non :orphan-value]])))

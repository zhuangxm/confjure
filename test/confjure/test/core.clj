(ns confjure.test.core
  (:use [confjure.core :reload-all true]
        [clojure.test]))

(deftest test-value-legal?
  (let [m {:myconf {:pred identity}}]
    (are [k v expected] (= (value-legal? m k v) expected)
         :myconf true :legal
         :myconf false :illegal
         :hisconf true nil)))

(deftest test-check-values
  (let [elems {:myconf {:pred identity}, :conf2 {:pred #(= 3 %)}}]
    (are [values expected] (= (check-values values elems) expected)
         {:myconf true, :conf2 3} {:myconf :legal, :conf2 :legal}
         {:myconf true, :conf2 0} {:myconf :legal, :conf2 :illegal}
         {:conf2 3} {:myconf nil, :conf2 :legal}
         )))

(deftest test-translate-results
  (let [elems {:myconf {:doc "foo"}, :conf2 {:doc "bar"}}]
    (are [results expected]
         (= (set (translate-results elems results)) (set expected))
         {:myconf :legal, :conf2 :legal} []
         {:myconf :legal, :conf2 :illegal} ["illegal conf2: bar"]
         {:myconf :illegal, :conf2 :nonexist}
         ["illegal myconf: foo" "nonexist conf2: bar"])))

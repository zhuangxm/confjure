(ns confjure.init
  (:require [confjure.core :as core]
            [clojure.tools.namespace :as namespace]))

;;customize confjure unit.
;;all confjure dict define must be placed under confjure.dict namepsace

;;all production confjure config define must be placed under
;;clojure.conf namespace

;;all test confjure config define must be placed under
;;clojure.test-conf namespace

;;to avoid duplicate clj file, recommand to use library or project
;;name as dict and conf clj file name.

;;基本想法
;; dict and conf 在生产环境下只能配置一次.
;; dict and conf 在测试环境下可以任意配置.
;; 不再使用环境变量，只提供 测试和 生产两种模式, provide! function 参
;; 数简化。
;; dict and conf 集中配置.
;; 生产模式下可以第一时间检查配置问题.
;; dict item 增加所定义的命名空间，重复配置时给出更清楚的提示
;; conf item 同上， (未实现)

;;建议使用场景
;; 所有词典定义都放在 confure.dict 的命名空间下

;; 生产模式
;; 1 所有配置定义都放在 confjure.conf 的命名空间下
;; 2 在启动代码中 调用 (load-confg) 初始化配置环境

;; 测试和开发模式下
;; 1 provide! function 只写在测试代码中或者confjure.test-confg 空间下
;; 2 provide! function 不写在任何逻辑代码中
;; 3 测试代码中先调用 (load-test-confg) 初始化测试配置环境
;; 4 在测试代码中可以任意 调用 provide! function.

(defn load-ns
  "Require all the namespaces prefixed by the namespace symbol given so that the dict and conf are loaded."
  [& ns-syms]
  (doseq [ns-sym ns-syms
          n (namespace/find-namespaces-on-classpath)
          :let [pattern (re-pattern (name ns-sym))]
          :when (re-seq pattern (name n))]
    (require n)))

(defn set-test-mode!
  [is-test]
  (swap! core/test-mode is-test))

(defn load-test-conf
  "load all dict item and test config and check all config value"
  []
  (do 
    (set-test-mode true)
    (core/clear-confjure!)
    (load-ns 'confjure.dict)
    (load-ns 'confjure.test-conf)
    (core/check-all)))

(defn load-conf
  "load all dict item and production config and check all config value"
  []
  (do 
    (set-test-mode false)
    (core/clear-confjure!)
    (load-ns 'confjure.dict)
    (load-ns 'confjure.conf)
    (core/check-all)))

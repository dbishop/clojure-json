(ns clojure-json
  (:require (org.danlarkin [json :as json]))
  (:use (clojure.contrib [test-is :as test-is])))

;setup JSON encoder-decoder checker test
(defmethod test-is/assert-expr :json=
  [msg form]
  `(let [values# (list ~@(rest form))
         json-form# (first values#)
         json-string# (json/encode-to-str json-form#)
         decoded-string# (json/decode-from-str json-string#)
         result# (= json-form# decoded-string#)]
     (if result#
       (report :pass ~msg '~form `(~'~'= ~json-form# ~decoded-string#))
       (report :fail ~msg
               `(~'~'= ~json-form# ~decoded-string#)
               (list '~'not= json-form# decoded-string#)))
     result#))



;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;        Basics         ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;
(deftest array
  (is (:json= [1 2 3 4 5])))

(deftest empty-object
  (is (:json= {})))

(deftest single-object
  (is (:json= {:foo 1})))

(deftest double-object
  (is (:json= {:bar 2 :foo 1})))

(deftest nested-array
  (is (:json= [[[[[[1 2 3 4 5]]]]]])))

(deftest nested-object
  (is (:json= {:bam {:foo 1} :baz {:bar 2}})))

(deftest array-of-objects
  (is (:json= [{:foo 1} {:bar 2}])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;       Numbers         ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;
(deftest easy-number
  (is (:json= 10)))

(deftest float-number
  (is (:json= 10.01)))

(deftest big-integer
  (is (:json= 100000000000000000000000000000)))

(deftest big-float
  (is (:json= 100000000000000000000000000000.01)))

(deftest small-exponent
  (is (:json= 1e2)))

(deftest big-exponent
  (is (:json= 1e25)))

(deftest negative-exponent
  (is (:json= 1e-2)))

(deftest plus-sign-exponent
  (is (:json= 1e+2)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;       Unicode         ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;
(deftest unicode-chars
  (is (:json= "ٯ✈")))

(deftest escaped-unicode-chars
  (is (:json= "\u066f\u2708")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;       Indenting       ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;
(deftest array-of-objects
  (is (= (json/encode-to-str [{:foo 1},{:bar 2}] :indent 2)
         "[\n  {\n    \"foo\":1\n  },\n  {\n    \"bar\":2\n  }\n]")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;      All-in-one       ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;
; from http://www.json.org/JSON_checker/test/pass1.json
(deftest pass1
  (let [string (slurp "test/pass1.json")
        decoded-json (json/decode-from-str string)]))
    ;    encoded-json (json/encode-to-str decoded-json)
    ;    re-decoded-json (json/decode-from-str encoded-json)]
    ;(is (= decoded-json re-decoded-json))))



(run-tests)

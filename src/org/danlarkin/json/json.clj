;; Copyright (c) 2008 Dan Larkin
;; All rights reserved.

;; Redistribution and use in source and binary forms, with or without
;; modification, are permitted provided that the following conditions
;; are met:
;; 1. Redistributions of source code must retain the above copyright
;;    notice, this list of conditions and the following disclaimer.
;; 2. Redistributions in binary form must reproduce the above copyright
;;    notice, this list of conditions and the following disclaimer in the
;;    documentation and/or other materials provided with the distribution.
;; 3. The name of the author may not be used to endorse or promote products
;;    derived from this software without specific prior written permission.

;; THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
;; IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
;; OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
;; IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
;; INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
;; NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
;; DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
;; THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
;; (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
;; THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

(ns org.danlarkin.json
  (:require (clojure.contrib [str-utils :as str-utils])))

(def encode) ;encode is used before it's defined, so we have to
             ;pre-define it

(defn- create-hash-pairs
  "Helper function for encoding maps.
   Returns a vector of string key:value pairs"
  [hmap]
  (loop [dict hmap
         accumulator []]
    (if (= (first dict) nil)
      accumulator
      (let [k (ffirst dict)
            v (second (first dict))]
        (recur (rest dict)
               (conj accumulator 
                     (str (encode k)
                          ":"
                          (encode v))))))))

(defn- encode-map
  [hmap & opts]
  (let [opts (apply hash-set opts)
        pad (if (:pad opts) \newline "")
        indent (if (:pad opts) "   " "")]
    (str \{
         pad 
         (str-utils/str-join (str "," pad)
                             (map #(str indent %) (create-hash-pairs hmap)))
         pad
         \})))

(defn- encode-coll
  [lst & opts]
  (let [opts (apply hash-set opts)
        pad (if (:pad opts) \newline "")
        indent (if (:pad opts) "   " "")]
    (str \[
         pad
         (str-utils/str-join (str "," pad)
                             (map #(str indent (encode %)) lst))
         pad
         \])))

(defn encode
  "This is the only function exported from this namespace.
   It takes an arbitrarily nested clojure datastructure
   and returns a JSON-encoded string representation."
  [value & opts]
  (cond
   (= (class value) java.lang.Boolean) value
   (nil? value) 'null
   (string? value) (str \" value \")
   (number? value) value
   (symbol? value) (str \" value \")
   (keyword? value) (str \" value \")
   (map? value) (apply encode-map value opts)
   (coll? value) (apply encode-coll value opts)
   :else (throw (Exception. "Unknown Datastructure"))))

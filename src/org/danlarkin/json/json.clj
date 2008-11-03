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
  (:import (java.io Writer StringWriter))
  (:use org.danlarkin.json.writer-utils))

(def encode) ;encode is used before it's defined, so we have to
             ;pre-define it

(defn- create-hash-pairs
  "Helper function for encoding maps.
   Returns a vector of string key:value pairs"
  [hmap]
  (let [writer (StringWriter.)]
    (loop [dict hmap
           accumulator []]
      (if (= (first dict) nil)
        accumulator
        (let [k (ffirst dict)
              v (second (first dict))]
          (recur (rest dict)
                 (conj accumulator
                       (writer-append writer
                                      (encode k)
                                      ":"
                                      (encode v)))))))))

(defn- encode-map
  [hmap #^Writer writer & opts]
  (let [opts (apply hash-set opts)
        pad (if (:pad opts) \newline "")
        indent (if (:pad opts) "   " "")]
    (writer-append writer \{ pad)
    (writer-join writer (str "," pad)
                 (for [x (create-hash-pairs hmap)]
                   (str indent x)))
    (writer-append writer pad \})))

(defn- encode-coll
  [lst #^Writer writer & opts]
  (let [opts (apply hash-set opts)
        pad (if (:pad opts) \newline "")
        indent (if (:pad opts) "   " "")]
    (writer-append writer \[ pad)
    (writer-join writer
                 (str "," pad)
                 (for [x lst]
                   (str indent (encode x))))
    (writer-append writer pad \])))

(defn- encode-helper
  [value #^Writer writer & opts]
  (cond
   (= (class value) java.lang.Boolean) (writer-append writer value)
   (nil? value) (writer-append writer 'null)
   (string? value) (writer-append writer \" value \")
   (number? value) (writer-append writer value)
   (symbol? value) (writer-append writer \" value \")
   (keyword? value) (writer-append writer \" value \")
   (map? value) (apply encode-map value writer opts)
   (coll? value) (apply encode-coll value writer opts)
   :else (throw (Exception. "Unknown Datastructure"))))

(defn encode
  "This is the only function exported from this namespace.
   It takes an arbitrarily nested clojure datastructure
   and returns a JSON-encoded string representation."
  ([value & opts]
     (let [w (StringWriter.)]
       (apply encode-helper value w opts))))

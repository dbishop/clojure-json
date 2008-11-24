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
  (:import (java.io Writer StringWriter)))

;(set! *warn-on-reflection* true)

(def
 #^{:private true}
 separator-symbol         ;separator-symbol will be used for encoding
 (symbol ","))            ;commas in arrays and objects: [a,b,c] and {a:b,c:d}

(defn- encode-helper [])  ;encode-helper is used before it's defined
                          ;so we have to pre-define it

(defn- map-entry?
  "Returns true if x is a MapEntry"
  [x]
  (instance? clojure.lang.IMapEntry x))

(defn- get-next-indent
  "Returns a string of size (+ (count current-indent) indent-size)
   iff indent-size is not zero."
  [#^String current-indent #^Integer indent-size]
  (if (zero? indent-size)
    ""
    (str current-indent
         (apply str (replicate indent-size " ")))))

(defn- start-token
  [#^clojure.lang.IPersistentCollection coll]
  (if (map? coll)
    \{
    \[))

(defn- end-token
  [#^clojure.lang.IPersistentCollection coll]
  (if (map? coll)
    \}
    \]))

(defn- encode-symbol
  "Encodes a symbol into JSON.
   If that symbol happens to be separator-symbol, though,
   it will be encoded without surrounding quotation marks."
  [#^clojure.lang.Symbol value #^Writer writer #^String pad]
  (if (= value separator-symbol)
    (. writer (append (str separator-symbol pad)))
    (. writer (append (str \" value \")))))

(defn- encode-map-entry
  "Encodes a single key:value pair into JSON."
  [#^clojure.lang.MapEntry pair #^Writer writer
   #^String pad #^String current-indent #^Integer indent-size]
  (let [next-indent (get-next-indent current-indent indent-size)]
    (encode-helper (key pair) writer pad current-indent indent-size)
    (. writer (append ":"))
    (encode-helper (val pair) writer pad "" indent-size next-indent)))

(defn- encode-coll
  "Encodes a collection into JSON."
  [#^clojure.lang.IPersistentCollection coll #^Writer writer
   #^String pad #^String current-indent #^String start-token-indent #^Integer indent-size]
  (let [end-token-indent (apply str (drop indent-size current-indent))
        next-indent (get-next-indent current-indent indent-size)]
    (. writer (append (str start-token-indent (start-token coll) pad)))
    (dorun (map (fn [x]
                  (encode-helper x writer pad current-indent indent-size))
                (interpose separator-symbol coll)))
    (. writer (append (str pad end-token-indent (end-token coll))))))


(defmulti encode-custom
  ;Multimethod for encoding classes of objects that
  ;aren't handled by the default encode-helper.
  (fn [value & _] (class value)))

(defmethod
  #^{:private true}
  encode-custom :default
  [value & _]
  (throw (Exception. (str "Unknown Datastructure: " value))))

(defn encode-helper
  [value #^Writer writer #^IPersistentMap
   #^String pad #^String current-indent #^Integer indent-size & opts]
  (let [next-indent (if-let [x (first opts)]
                      x
                      (get-next-indent current-indent indent-size))]
    (cond
     (= (class value) java.lang.Boolean) (. writer (append (str current-indent value)))
     (nil? value) (. writer (append (str current-indent 'null)))
     (string? value) (. writer (append (str current-indent \" value \")))
     (number? value) (. writer (append (str current-indent value)))
     (keyword? value) (. writer (append (str current-indent \" (name value) \")))
     (symbol? value) (encode-symbol value writer pad)
     (map-entry? value) (encode-map-entry value writer pad current-indent indent-size)
     (coll? value) (encode-coll value writer pad next-indent current-indent indent-size)
     :else (encode-custom value writer pad next-indent current-indent indent-size))))

(defn encode-to-str
  "Takes an arbitrarily nested clojure datastructure
   and returns a JSON-encoded string representation
   in a java.lang.String."
  [value & opts]
  (let [writer (StringWriter.)
        opts (apply hash-map opts)
        indent-size (get opts :indent 0)
        pad (if (> indent-size 0) \newline "")
        indent (apply str (replicate indent-size " "))]
    (str (encode-helper value writer pad "" indent-size))))

(defn encode-to-writer
  "Takes an arbitrarily nested clojure datastructure
   and a java.io.Writer and returns a JSON-encoded
   string representation in the java.io.Writer."
  [value #^Writer writer & opts]
  (let [opts (apply hash-map opts)
        indent-size (get opts :indent 0)
        pad (if (> indent-size 0) \newline "")
        indent (apply str (replicate indent-size " "))]
    (encode-helper value writer pad "" indent-size)))

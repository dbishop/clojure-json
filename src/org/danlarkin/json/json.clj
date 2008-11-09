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

(set! *warn-on-reflection* true)

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
  [#^clojure.lang.Symbol value #^Writer writer]
  (if (= value separator-symbol)
    (. writer (append (str separator-symbol)))
    (. writer (append (str \" value \")))))

(defn- encode-map-entry
  [#^clojure.lang.MapEntry pair #^Writer writer
   #^String pad #^String indent #^Integer indent-size]
  (encode-helper (key pair) writer pad "" indent-size)
  (. writer (append ":"))
  (encode-helper (val pair) writer pad "" indent-size))

(defn- encode-coll
  "Encodes a collection into JSON."
  [#^clojure.lang.IPersistentCollection coll #^Writer writer
   #^String pad #^String indent #^Integer indent-size]
  (. writer (append (str (start-token coll) pad)))
  (let [ind (if (empty? indent) "" (str indent
                                        (apply str (replicate indent-size " "))))]
    (dorun (map (fn [x]
                  (. writer (append indent))
                  (encode-helper x writer pad indent indent-size))
                (interpose separator-symbol coll))))
  (. writer (append (str pad (end-token coll)))))

(defn- encode-helper
  [value #^Writer writer
   #^String pad #^String indent #^Integer indent-size]
  (let [ind (str indent
                 (apply str (replicate indent-size " ")))]
    (cond
     (= (class value) java.lang.Boolean) (. writer (append (str ind value)))
     (nil? value) (. writer (append (str ind 'null)))
     (string? value) (. writer (append (str ind \" value \")))
     (number? value) (. writer (append (str ind value)))
     (keyword? value) (. writer (append (str ind \" value \")))
     (symbol? value) (encode-symbol value writer)
     (map-entry? value) (encode-map-entry value writer pad ind indent-size)
     (coll? value) (encode-coll value writer pad ind indent-size)
     :else (throw (Exception. (str "Unknown Datastructure: " value))))))

(defn encode-to-str
  "Takes an arbitrarily nested clojure datastructure
   and returns a JSON-encoded string representation
   in a java.lang.String."
  [value & opts]
  (let [writer (StringWriter.)
        opts (apply hash-map opts)
        pad (if (:pad opts) \newline "")
        indent-size (get opts :indent 0)
        indent (apply str (replicate indent-size " "))]
    (str (encode-helper value writer pad "" indent-size))))

(defn encode-to-writer
  "Takes an arbitrarily nested clojure datastructure
   and a java.lang.Writer and returns a JSON-encoded
   string representation in the java.io.Writer."
  [value #^Writer writer & opts]
  (let [opts (apply hash-map opts)
        pad (if (:pad opts) \newline "")
        indent-size (get opts :indent 0)
        indent (apply str (replicate indent-size " "))]
    (encode-helper value writer pad "" indent-size)))

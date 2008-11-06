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

(defn- encode-helper []) ;encode-helper is used before it's defined
                         ;so we have to pre-define it

(defn- encode-map
  "Encodes a hash-map into JSON.
   Has a caveat that any strings that evaluate to the
   (gensym 'comma__') will be encoded as commas."
  [hmap #^Writer writer #^String pad #^String indent #^Integer indent-size]
  (. writer (append (str \{ pad)))
  (let [comma (gensym "comma__")
        ind (if (empty? indent) "" (apply str (replicate indent-size " ")))]
    (dorun (map (fn [x] (if (= x comma)
                          (do
                            (. writer (append ","))
                            (. writer (append pad)))
                          (do
                            (encode-helper (first x) writer pad ind indent-size)
                            (. writer (append ":"))
                            (encode-helper (second x) writer pad "" 0))))
                (interpose comma hmap))))
  (. writer (append (str indent pad \}))))

(defn- encode-coll
  "Encodes a collection into JSON.
   Has a caveat that any strings that evaluate to the
   (gensym 'comma__') will be encoded as commas."
  [lst #^Writer writer #^String pad #^String indent #^Integer indent-size]
  (. writer (append (str \[ pad)))
  (let [comma (gensym "comma__")
        ind (if (empty? indent) "" (str indent
                                        (apply str (replicate indent-size " "))))]
    (dorun (map (fn [x]
                  (if (= x comma)
                    (do
                      (. writer (append ","))
                      (. writer (append pad)))
                    (do
                      (. writer (append indent))
                      (encode-helper x writer pad indent indent-size))))
                (interpose comma lst))))
  (. writer (append (str pad \]))))

(defn- encode-helper
  [value #^Writer writer #^String pad #^String indent #^Integer indent-size]
  (let [ind (str indent
                 (apply str (replicate indent-size " ")))]
    (cond
     (= (class value) java.lang.Boolean) (. writer (append (str ind value)))
     (nil? value) (. writer (append (str ind 'null)))
     (string? value) (. writer (append (str ind \" value \")))
     (number? value) (. writer (append (str ind value)))
     (symbol? value) (. writer (append (str ind \" value \")))
     (keyword? value) (. writer (append (str ind \" value \")))
     (map? value) (encode-map value writer pad ind indent-size)
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

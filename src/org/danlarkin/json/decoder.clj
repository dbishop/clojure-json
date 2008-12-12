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

(ns org.danlarkin.json.decoder
  (:import (java.io StringReader StreamTokenizer)))

(declare decode-value)   ;decode-value is used before it's defined
                         ;so we have to pre-define it

(defn- convert-number
  "Converts x to an int iff it can be done without losing significance"
  [x]
  (let [ix (int x)]
    (if (= ix x)
      ix
      x)))

(defn- check-parsing-accuracy
  "A bug (or what I assume must be a bug) in Java's StreamTokenizer
   prevents it from correctly handling numbers in the exponential
   form.  So if the parser is passed a number and the next token
   starts with ^[eE]-? then we know to pull from the future and
   get the exponent."
  [this next]
  (and
   (number? this)
   (and (string? next) (re-find #"^[eE]-?" next);got an exponent
        (if (re-find #"[0-9]$" next);exponent ends with a number
          :parsed-ok
          :parsed-bad))))

(defn- decode-array
  "Given a token-seq, return a vector and the new 'head' of the token-seq"
  [token-seq]
  (loop [array []
	 next-token (first token-seq)
	 value token-seq]
    (if (= next-token \]) [array (rest value)]
	(let [[decoded-value post-value-seq] (decode-value value)]
	  (recur (if (= \, decoded-value)
                   array
                   (conj array decoded-value))
		 (first post-value-seq)
		 post-value-seq)))))

(defn- decode-object
  "Given a token-seq, return a hash-map and the new 'head' of the token-seq"
  [token-seq]
  (loop [object {}
         key (first token-seq)
         value (rest token-seq)]
    (if (= key \}) [object value]
	(let [[decoded-value post-value-seq] (decode-value value)]
          (if (= \, key)
            (recur object
                   (first value)
                   (rest value))
            (recur (assoc object (keyword (str key)) decoded-value)
                   (first post-value-seq)
                   (rest post-value-seq)))))))

(defn- decode-value
  "Given a token-seq, return a value (string, number, object, array, true, false, nil)"
  [token-seq]
  (let [next-token (first token-seq)
	new-seq (rest token-seq)
	peek-ahead (first new-seq)]
    (cond
     (= next-token \{) (decode-object new-seq)
     (= next-token \[) (decode-array new-seq)
     (= next-token "true") [true new-seq]
     (= next-token "false") [false new-seq]
     (= next-token "null") [nil new-seq]
     ;tokenizer screws up exponentiated numbers, so fix them here
     (= (check-parsing-accuracy next-token peek-ahead) :parsed-ok)
       [(Double. (str next-token peek-ahead)) (rest new-seq)]
     (= (check-parsing-accuracy next-token peek-ahead) :parsed-bad)
       [(Double. (str next-token peek-ahead (frest new-seq))) (rrest new-seq)]
     :else [next-token new-seq])))

(defn- token-seq-builder
  "Returns a seq of tokens as vectors like [<nextToken_return> <value>]"
  [tokenizer]
  (fnseq (let [_ (.nextToken tokenizer)
	       ttype (.ttype tokenizer)]
	   (cond (= ttype StreamTokenizer/TT_EOF) "TT_EOF"
		 (= ttype StreamTokenizer/TT_EOL) "TT_EOL"
		 (= ttype StreamTokenizer/TT_NUMBER) (convert-number (.nval tokenizer))
		 (= ttype StreamTokenizer/TT_WORD) (.sval tokenizer)
		 (= ttype 34) (.sval tokenizer)
		 :else (char ttype)))
         #(token-seq-builder tokenizer)))

(defn decode-helper
  [reader]
  (let [tokenizer (doto (StreamTokenizer. reader)
		    (.eolIsSignificant false)
		    (.lowerCaseMode false)
		    (.slashStarComments false)
		    (.slashSlashComments false)
		    (.whitespaceChars 0 33) ; (up to and including !)
		    (.whitespaceChars 35 43) ; #$%&'()*+
		    (.whitespaceChars 47 47) ; /
		    (.whitespaceChars 58 64) ; :;<=>?@
		    (.whitespaceChars 92 92) ; \
		    (.whitespaceChars 94 96) ; ^_`
		    (.whitespaceChars 124 124) ; |
		    (.whitespaceChars 126 254) ; ~ del etc...
		    (.ordinaryChar 123) ; {
		    (.ordinaryChar 125) ; }
		    (.ordinaryChar 91) ; [
		    (.ordinaryChar 93) ; ]
                    (.ordinaryChar 44) ; ,
		    (.quoteChar 34) ; "
		    (.parseNumbers))
	token-seq (token-seq-builder tokenizer)]
    (cond
     (= (first token-seq) \{) (first (decode-object (rest token-seq)))
     (= (first token-seq) \[) (first (decode-array (rest token-seq)))
     :else (first (decode-value token-seq)))))

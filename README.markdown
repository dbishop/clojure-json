# Installing #

* Add the src directory to your classpath.
* Reference it with something like  
  `(ns foo (:require (org.danlarkin [json :as json])))`

# Using #

    foo=> (print (json/encode-to-str [1 2 3 4 5]))
    [1,2,3,4,5]nil
    foo=> (print (json/encode-to-str {"a" 1 "b" 2 "c" 3}))
    {"a":1,"b":2,"c":3}nil
    foo=> (print (json/encode-to-str [1 2 3 4 5] :indent 2))
    [
      1,
      2,
      3,
      4,
      5
    ]nil
    foo=> (import '(java.io FileWriter))
    nil
    foo=> (. (json/encode-to-writer [1 2 3 4 5] (FileWriter. "/tmp/foo.json")) (close))
    nil
    foo=> (import '(java.util Date))
    nil
    foo=> (json/encode-to-str (Date.))
    java.lang.Exception: Unknown Datastructure: Sat Nov 5 19:00:00 GMT 1605 (NO_SOURCE_FILE:0)
    foo=> (defmethod json/encode-custom java.util.Date
            [#^java.util.Date date #^Writer writer
             #^String pad #^String current-indent #^String start-token-indent #^Integer indent-size]
            (.append writer (str start-token-indent \" date \")))
    #<MultiFn clojure.lang.MultiFn@42e23f>
    foo=> (print (json/encode-to-str (Date.)))
    "Sat Nov 5 19:00:00 GMT 1605"nil
    foo=> (print (json/encode-to-str [(Date.) (Date.) (Date.)] :indent 2))
    [
      "Sat Nov 5 19:00:00 GMT 1605",
      "Sat Nov 5 19:00:00 GMT 1605",
      "Sat Nov 5 19:00:00 GMT 1605"
    ]nil
    foo=> (print (json/encode-to-str {:foo (Date.) :bam 4 :quux 'bar} :indent 2))
    {
      "quux":"bar",
      "foo":"Sat Nov 5 19:00:00 GMT 1605",
      "bam":4
    }nil
    
# Custom Encoding #
`clojure-json` uses a [multimethod](http://clojure.org/multimethods) for custom encoding, dispatching on type.  
If you're adding an encoder function for a container type make sure to respect all of the indentation arguments
that your function will be passed and to call encode-helper on each of the elements in your container.  
Also note that when calling defmethod you have to qualify encode-custom with the namespace in which it's defined.

# Note #

This package can't decode json currently, only encode.  
There is, however, a decoder available on the clojure  
mailing list here:
http://groups.google.com/group/clojure/browse_thread/thread/c25325debf4c0d78  
Hopefully soon I'll add decoding capability.
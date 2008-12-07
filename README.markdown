# Installing #

* Add the src directory to your classpath.
* Reference it with something like  
  `(ns foo (:require (org.danlarkin [json :as json])))`

# Using The Encoder #

    foo=> (print (json/encode-to-str [1 2 3 4 5]))
    [1,2,3,4,5]nil
    foo=> (print (json/encode-to-str {:a 1 :b 2 :c 3}))
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
    foo=> (json/encode-to-writer [1 2 3 4 5] (FileWriter. "/tmp/foo.json"))
    #<FileWriter java.io.FileWriter@c93f91>
    foo=> (import '(java.util Date))
    nil
    foo=> (json/encode-to-str (Date.))
    java.lang.Exception: Unknown Datastructure: Sat Nov 5 19:00:00 GMT 1605 (NO_SOURCE_FILE:0)
    foo=> (defn date-encoder
            [date writer pad current-indent start-token-indent indent-size]
            (.append writer (str start-token-indent \" date \")))
    #'foo/date-encoder
    foo=> (json/add-encoder java.util.Date date-encoder)
    #<MultiFn clojure.lang.MultiFn@da6c0d>
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
      "foo":"Sat Nov 5 19:00:00 GMT 1605",
      "bam":4,
      "quux":"bar"
    }nil
    
# Custom Encoding #
`clojure-json` uses a [multimethod](http://clojure.org/multimethods) for custom encoding, dispatching on type.  
If you're adding an encoder function for a container type make sure to respect all of the indentation arguments
that your function will be passed and to call encode-helper on each of the elements in your container.  
I've left the parameters to date-encoder un-hinted in this example in the interest of brevity but their inclusion
does seem to speed up execution time a good bit so I suggest using them where speed matters.

# Using The Parser #

    foo=> (json/decode-from-str "[1, 2, 3, 4, 5]")
    [1 2 3 4 5]
    foo=> (json/decode-from-str "{\"foo\":1, \"bar\":2, \"baz\":3}")
    {:foo 1, :bar 2, :baz 3}
    foo=> (json/decode-from-str "{\"foo\":[1,2,\"superbam\"], \"bar\":{\"bam\":98.6}, \"baz\":3}")
    {:foo [1 2 "superbam"], :bar {:bam 98.6}, :baz 3}
    foo=> (json/decode-from-reader (FileReader. "/tmp/foo.json"))
    [1 2 3 4 5]

# Special Thanks #
Special thanks go to Darrell Bishop of arubanetworks.com for writing the parser included in this distribution.
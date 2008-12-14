# Installing #

There are two ways to install clojure-json:

* Use `ant` to compile to a JAR:
  * To package .class files and .clj sources use: `ant -Dclojure.jar=/path/to/clojure.jar`
  * To package only .clj sources use: `ant`
  * Add `clojure-json.jar` to your classpath
* Just add the `src` directory to your classpath

# Using The Encoder #

    user=> (require '(org.danlarkin [json :as json]))
    nil
    user=> (print (json/encode-to-str [1 2 3 4 5]))
    [1,2,3,4,5]nil
    user=> (print (json/encode-to-str {:a 1 :b 2 :c 3}))
    {"a":1,"b":2,"c":3}nil
    user=> (print (json/encode-to-str [1 2 3 4 5] :indent 2))
    [
      1,
      2,
      3,
      4,
      5
    ]nil
    user=> (import '(java.io FileWriter))
    nil
    user=> (json/encode-to-writer [1 2 3 4 5] (FileWriter. "/tmp/foo.json"))
    #<FileWriter java.io.FileWriter@c93f91>
    user=> (import '(java.util Date))
    nil
    user=> (json/encode-to-str (Date.))
    java.lang.Exception: Unknown Datastructure: Sat Nov 5 19:00:00 GMT 1605 (NO_SOURCE_FILE:0)
    user=> (defn date-encoder
            [date writer pad current-indent start-token-indent indent-size]
            (.append writer (str start-token-indent \" date \")))
    #'user/date-encoder
    user=> (json/add-encoder java.util.Date date-encoder)
    #<MultiFn clojure.lang.MultiFn@da6c0d>
    user=> (print (json/encode-to-str (Date.)))
    "Sat Nov 5 19:00:00 GMT 1605"nil
    user=> (print (json/encode-to-str [(Date.) (Date.) (Date.)] :indent 2))
    [
      "Sat Nov 5 19:00:00 GMT 1605",
      "Sat Nov 5 19:00:00 GMT 1605",
      "Sat Nov 5 19:00:00 GMT 1605"
    ]nil
    user=> (print (json/encode-to-str {:foo (Date.) :bam 4 :quux 'bar} :indent 2))
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

    user=> (require '(org.danlarkin [json :as json]))
    nil
    user=> (json/decode-from-str "[1, 2, 3, 4, 5]")
    [1 2 3 4 5]
    user=> (json/decode-from-str "{\"foo\":1, \"bar\":2, \"baz\":3}")
    {:foo 1, :bar 2, :baz 3}
    user=> (json/decode-from-str "{\"foo\":[1,2,\"superbam\"], \"bar\":{\"bam\":98.6}, \"baz\":3}")
    {:foo [1 2 "superbam"], :bar {:bam 98.6}, :baz 3}
    user=> (json/decode-from-reader (FileReader. "/tmp/foo.json"))
    [1 2 3 4 5]

# Special Thanks #
Special thanks go to Darrell Bishop of arubanetworks.com for writing the parser included in this distribution.
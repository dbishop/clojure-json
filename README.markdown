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

# Note #

This package can't decode json currently, only encode.  
There is, however, a decoder available on the clojure  
mailing list here:
http://groups.google.com/group/clojure/browse_thread/thread/c25325debf4c0d78  
Hopefully soon I'll add decoding capability.
# twisk

A Clojure library designed to extract data from e-mail messages in an idiomatic way.

## Install

Not published to clojars yet.

## Usage


```clojure
(require '[twisk.message :as email])

;; Given m a javax.mail.internet.MimeMessage

(let [from (email/from m)
      to (email/to m)
      subj (email/subject m)]
    (if (email/multipart? m)
        (doseq [part (email/parts-of m)]
            (println (email/content-type m))
        (println "Is single part")))
      
```

## About the name

Twisk is a chaotic fairy appearing in the Lyonesse trilogy by Jack Vance. 

## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

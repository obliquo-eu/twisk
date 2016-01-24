(ns twisk.message-test
  (:require [clojure.test :refer :all]
            [twisk.message :refer :all]
            [twisk.fixtures :refer [make-simple-email make-alternative-email]]
            [clojure.zip :as z]))

(deftest mimeentity-test
  (testing "Simple body text/plain"
    (let [msg-body "body"
          m (make-simple-email "from@example.org"
                               "to@example.org"
                               "subject"
                               msg-body)]
      (is (= "text/plain" (content-type m)) "content type should be text/plain")
      (is (= msg-body (body m)) "the body should be equal to the defined one")))
  (testing "Simple body in text/html"
    (let [msg-body "<html><body>Hey</body></html>"
          m (make-simple-email "from@example.org"
                               "to@example.org"
                               "subject"
                               msg-body
                               :type "text/html")]
      (is (= "text/html" (content-type m)) "content type should be text/html")
      (is (= msg-body (body m)) "body should be equal to the defined one")))

  (testing "Multipart alternative body"
    (let [msg-body-lr "body"
          msg-body-hr "<html><body>Body</body></html>"
          m (make-alternative-email "from@example.org"
                                    "to@example.org"
                                    "subject"
                                    ["text/plain" msg-body-lr]
                                    ["text/html" msg-body-hr])]
      (is (multipart? m) "the message should be classified as multipart")
      (is (= "multipart/alternative" (content-type m)) "content-type should be multipat/alternative")
      (is (= 2 (count (parts-of m))) "The message should have two parts")
      (is (= "text/plain" (content-type (first (parts-of m)))) "first part is text/plain")
      (is (= "text/html" (content-type (second (parts-of m)))) "second part is text/html")
      (is (= msg-body-lr (body (first (parts-of m)))) "first part body equals the defined one")
      (is (= msg-body-hr (body (second (parts-of m)))) "second part body equals the defined one")
      (is (= msg-body-lr (slurp (body-stream (first (parts-of m))))) "first part body as a stream equals the defined one")))
  (testing "Non-default character encoding"
    (let [msg-body "1000 €"
          m (make-simple-email "a"
                               "b"
                               "price"
                               msg-body
                               :enc "iso-8859-15")]
      (is (= msg-body (body m)) "Message body matches the defined one"))))

(deftest ro-zipper-test
  (testing "simple message"
    (let [m (make-simple-email "a" "b" "c" "d")
          zm (ro-zipper m)]
      (is (= m (z/node zm)) "extracted node from the root position should be equal to the message")
      (is (-> zm z/next z/end?) "the depth-first walk should have one node only")))
  (testing "multipart/alternative message"
    (let [msg-body-lr "body"
          msg-body-hr "<html><body>Body</body></html>"
          m (make-alternative-email "from@example.org"
                                    "to@example.org"
                                    "subject"
                                    ["text/plain" msg-body-lr]
                                    ["text/html" msg-body-hr])
          mz (ro-zipper m)]
      (is (= "text/plain" (-> mz z/down z/node content-type)) "first object after down should be plain")
      (is (= "text/html" (-> mz z/down z/right z/node content-type)) "sibling of the first node should be html")
      (is (-> mz z/next z/next z/next z/end?) "the walk should have length 3"))))

(deftest email-test
  (testing "simple message"
    (let [m (make-simple-email "f" "t" "s" "body")]
      (is (= "s" (subject m)) "subject returns the message subject")
      (is (= ["t"] (mapv address (to m))) "to returns TO addresses as a vector")
      (is (= ["f"] (mapv address (from m))) "cc returns CC addresses as a vector")
      (is (= (to m) (:to (recipients m))) "recipients returns a map which includes :to")
      (is (= (cc m) (:cc (recipients m))) "recipients returns a map which includes :cc")
      (is (= (bcc m) (:bcc (recipients m))) "recipients returns a map which includes :bcc"))))

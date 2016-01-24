(ns twisk.core-test
  (:require [clojure.test :refer :all]
            [twisk.core :refer :all]))

#_(deftest message-matching-test
  (testing "From match"
    (is ((m/match (m/from "user1@example.org")) {:from "user1@example.org"}) "Equal from fields should match")
    (is (not ((m/match (m/from "user1@example.org")) {:from "user2@example.org"})) "Different from fields should not match"))
  (testing "Subject match"
    (is ((m/match (m/subject "Test subject")) {:subject "Test subject"}) "Equal subject fields should match")
    (is ((m/match (m/subject "Test subject")) {:subject "Fail subject"}) "Different subject fields should not match")))

(ns twisk.fixtures
  (:require [twisk.message :refer [make-session]])
  (:import [javax.mail Session Message Message$RecipientType Address]
           [javax.mail.internet MimeMessage MimeBodyPart MimeMultipart InternetAddress]))

(defn string->array-of-InternetAddress
  [s]
  (into-array Address [(InternetAddress. s)]))

(defn make-simple-email
  [f t s b & {:keys [type enc] :or {type "text/plain" enc "utf-8"}}]
  (let [type (if (#{"utf-8" "UTF-8"} enc) type (str type ";charset=" enc))
        sess (make-session)
        m (doto (MimeMessage. sess)
            (.addFrom (string->array-of-InternetAddress f))
            (.addRecipients Message$RecipientType/TO
                            (string->array-of-InternetAddress t))
            (.setSubject s)
            (.setContent b type)
            .saveChanges)]
    (MimeMessage. m)))
   
(defn make-alternative-email
  [f t s & xs]
  (let [sess (make-session)
        mpart (MimeMultipart. "alternative")
        m (doto (MimeMessage. sess)
            (.addFrom (string->array-of-InternetAddress f))
            (.addRecipients Message$RecipientType/TO
                            (string->array-of-InternetAddress t))
            (.setSubject s))]
    (doseq [[c-type ctnt]  xs
            :let [part (doto (MimeBodyPart. )
                         (.setContent ctnt c-type))]]
      (.addBodyPart mpart part))
    (doto m
      (.setContent mpart)
      .saveChanges)
    (MimeMessage. m)))

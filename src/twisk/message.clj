(ns twisk.message
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.zip :as z])
  (:import [javax.mail Session]
           [javax.mail.internet MimeMessage MimePart MimeMultipart InternetAddress MimeMessage$RecipientType]
           [java.util Properties]
           (java.io InputStream)))

(defn make-session
  ([p] (Session/getInstance p))
  ([] (let [p (Properties. )]
        (make-session p))))
   
(defmulti load-mime type)

(defmethod load-mime :default
  [f]
  (with-open [is (io/input-stream f)]
    (load-mime is)))

(defmethod load-mime InputStream
  [is]
  (let [s (make-session)]
    (MimeMessage. s is)))

(defprotocol EmailAddress
  (address [self])
  (personal [self]))

(extend-type InternetAddress
  EmailAddress
  (address [self] (.getAddress self))
  (personal [self] (.getPersonal self)))

(defprotocol MIMEEntity
  (body [self])
  (body-stream [self])
  (content-id [self])
  (content-description [self])
  (content-type [self])
  (content-language [self])
  (content-disposition [self])
  (content-md5 [self])
  (multipart? [self])
  (parts-of [self]))
 
(defn- parts-vec
  "Returns a vector with the parts of a MimeMultiPart"
  [^MimeMultipart mp]
  (into [] (for [part-no (range (.getCount mp))] (.getBodyPart mp part-no))))

(extend-type MimePart
  MIMEEntity
  (body [self] (.getContent self))
  (body-stream [self] (.getInputStream self))
  (content-id [self] (.getContentID self))
  (content-description [self] (.getDescription self))
  (content-type [self] (if-let [res (-> (.getContentType self) (string/split #"\s*;\s+") first)] res "text/plain"))
  (content-language [self] (.getContentLanguage self))
  (content-disposition [self] (.getDisposition self))
  (content-md5 [self] (.getContentMD5 self))
  (multipart? [self] (not (nil? (re-matches #"^multipart.*$" (content-type self)))))
  (parts-of [self] (parts-vec (body self))))

(defn ro-zipper
  "Creates a read-only zipper able to traverse a Message or any MimePart"
  [^MimePart m]
  (letfn [(edit-fn [_ _] (throw (UnsupportedOperationException. "Editing not supported")))]
    (z/zipper multipart? parts-of edit-fn m)))

(defprotocol Email
  (from [self])
  (to [self])
  (cc [self])
  (bcc [self])
  (recipients [self])
  (subject [self]))

(extend-type MimeMessage
  Email
  (from [self] (into [] (.getFrom self)))
  (to [self] (into [] (.getRecipients self MimeMessage$RecipientType/TO)))
  (cc [self] (into [] (.getRecipients self MimeMessage$RecipientType/CC)))
  (bcc [self] (into [] (.getRecipients self MimeMessage$RecipientType/BCC)))
  (recipients [self] {:to (to self)
                      :cc (cc self)
                      :bcc (bcc self)})
  (subject [self] (.getSubject self)))

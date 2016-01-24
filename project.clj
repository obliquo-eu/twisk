(defproject twisk "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [javax.mail/javax.mail-api "1.5.5"]
                 [com.sun.mail/javax.mail "1.5.5"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.nrepl "0.2.12"]]
                   :plugins [[cider/cider-nrepl "0.11.0-SNAPSHOT"]]}})

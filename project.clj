(defproject oph.ehoks "0.1.0"
  :description "OPH eHOKS Backend"
  :min-lein-version "2.8.1"
  :pedantic? :abort
  :dependencies [[org.clojure/clojure]
                 [clj-http]
                 [com.layerware/hugsql]
                 [com.taoensso/carmine]
                 [metosin/compojure-api]
                 [org.flywaydb/flyway-core]
                 [org.postgresql/postgresql]
                 [ring/ring-jetty-adapter]]
  :managed-dependencies [[org.clojure/clojure "1.9.0"]

                         ;; http server
                         [javax.servlet/javax.servlet-api "4.0.1"]
                         [metosin/compojure-api "2.0.0-alpha23"]
                         [ring/ring-codec "1.1.1"]
                         [ring/ring-core "1.6.3"]
                         [ring/ring-jetty-adapter "1.6.3"]
                         [ring/ring-servlet "1.6.3"]

                         ;; http client
                         [clj-http "3.9.1"]
                         [org.apache.httpcomponents/httpasyncclient "4.1.4"]
                         [org.apache.httpcomponents/httpclient "4.5.6"]
                         [org.apache.httpcomponents/httpclient-cache "4.5.6"]
                         [org.apache.httpcomponents/httpcore "4.4.10"]
                         [org.apache.httpcomponents/httpcore-nio "4.4.10"]
                         [org.apache.httpcomponents/httpmime "4.5.6"]

                         ;; date, time
                         [joda-time "2.10"]

                         ;; json
                         [com.fasterxml.jackson.core/jackson-annotations "2.9.6"]
                         [com.fasterxml.jackson.core/jackson-core "2.9.6"]
                         [com.fasterxml.jackson.core/jackson-databind "2.9.6"]
                         [com.fasterxml.jackson.core/jackson-datatype-jsr310 "2.9.6"]

                         ;; postresql
                         [com.layerware/hugsql "0.4.9"]
                         [org.clojure/java.jdbc "0.7.8"]
                         [org.flywaydb/flyway-core "5.1.4"]
                         [org.postgresql/postgresql "42.2.5"]

                         ;; redis
                         [com.taoensso/carmine "2.18.1"]

                         ;; other
                         [commons-codec "1.11"]
                         [commons-fileupload "1.3.3"]
                         [commons-io "2.6"]
                         [hiccup "1.0.5"]
                         [org.clojure/tools.namespace "0.2.11"]]
  :plugins [[lein-cljfmt "0.6.0" :exclusions [org.clojure/tools.cli]]
            [lein-kibit "0.1.6"]
            [lein-bikeshed "0.5.1"]
            [jonase/eastwood "0.2.9"]
            [lein-auto "0.1.3"]
            [lein-ancient "0.6.15"]]
  :main oph.ehoks.main
  :aot [oph.ehoks.main]
  :uberjar-name "ehoks-standalone.jar"
  :source-paths ["src"]
  :aliases {"checkall" ["do"
                        ["kibit"]
                        ["bikeshed"]
                        ["eastwood"]
                        ["cljfmt" "check"]]}
  :cljfmt {:indents {#".*" [[:block 0]]}}
  :profiles {:dev {:main oph.ehoks.dev-server
                   :dependencies [[cheshire "5.8.0"]
                                  [ring/ring-mock "0.3.2"]
                                  [ring/ring-devel "1.7.0-RC2" :exclusions [ring/ring-core]]]
                   :resource-paths ["resources/dev" "resources/dev/src"]}})

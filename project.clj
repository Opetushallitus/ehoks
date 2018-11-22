(defproject oph.ehoks "0.1.1"
  :description "OPH eHOKS Backend"
  :min-lein-version "2.8.1"
  :pedantic? :abort
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-http "3.9.1"]
                 [com.layerware/hugsql "0.4.9"]
                 [com.taoensso/carmine "2.19.1"]
                 [metosin/compojure-api "2.0.0-alpha26"]
                 [org.flywaydb/flyway-core "5.2.1"]
                 [org.postgresql/postgresql "42.2.5"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [clj-time "0.15.1"]
                 [org.clojure/core.async "0.4.490"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.apache.logging.log4j/log4j-api "2.11.1"]
                 [org.apache.logging.log4j/log4j-core "2.11.1"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.11.1"]]
  :managed-dependencies [[org.clojure/clojure "1.9.0"]

                         ;; http server
                         [javax.servlet/javax.servlet-api "4.0.1"]
                         [metosin/compojure-api "2.0.0-alpha26"]
                         [ring/ring-codec "1.1.1"]
                         [ring/ring-core "1.7.1"]
                         [ring/ring-jetty-adapter "1.7.1"]
                         [ring/ring-servlet "1.7.1"]

                         ;; http client
                         [clj-http "3.9.1"]
                         [org.apache.httpcomponents/httpasyncclient "4.1.4"]
                         [org.apache.httpcomponents/httpclient "4.5.6"]
                         [org.apache.httpcomponents/httpclient-cache "4.5.6"]
                         [org.apache.httpcomponents/httpcore "4.4.10"]
                         [org.apache.httpcomponents/httpcore-nio "4.4.10"]
                         [org.apache.httpcomponents/httpmime "4.5.6"]

                         ;; logging
                         [org.clojure/tools.logging "0.4.1"]
                         [org.apache.logging.log4j/log4j-api "2.11.1"]
                         [org.apache.logging.log4j/log4j-core "2.11.1"]
                         [org.apache.logging.log4j/log4j-slf4j-impl "2.11.1"]

                         ;; date, time
                         [joda-time "2.10.1"]
                         [clj-time "0.15.1"]

                         ;; json
                         [com.fasterxml.jackson.core/jackson-annotations "2.9.7"]
                         [com.fasterxml.jackson.core/jackson-core "2.9.7"]
                         [com.fasterxml.jackson.core/jackson-databind "2.9.7"]
                         [com.fasterxml.jackson.core/jackson-datatype-jsr310 "2.9.7"]

                         ;; postresql
                         [com.layerware/hugsql "0.4.9"]
                         [org.clojure/java.jdbc "0.7.8"]
                         [org.flywaydb/flyway-core "5.2.1"]
                         [org.postgresql/postgresql "42.2.5"]

                         ;; redis
                         [com.taoensso/carmine "2.19.1"]

                         ;; other
                         [org.clojure/core.async "0.4.490"]
                         [commons-codec "1.11"]
                         [commons-fileupload "1.3.3"]
                         [commons-io "2.6"]
                         [hiccup "1.0.5"]
                         [org.clojure/tools.namespace "0.2.11"]

                         ;; Plugins
                         [org.clojure/tools.reader "1.3.2"]
                         [io.aviso/pretty "0.1.35"]
                         [instaparse "1.4.9"]]
  :plugins [[lein-cljfmt "0.6.0" :exclusions [org.clojure/tools.cli]]
            [lein-kibit "0.1.6"]
            [lein-bikeshed "0.5.1"]
            [jonase/eastwood "0.3.1"]
            [lein-auto "0.1.3"]
            [lein-ancient "0.6.15"]
            [lein-cloverage "1.0.13"]]
  :main oph.ehoks.main
  :aot [oph.ehoks.main]
  :uberjar-name "ehoks-standalone.jar"
  :source-paths ["src"]
  :resource-paths []
  :cloverage {;:fail-threshold 90
              :html? false}
  :bikeshed {:var-redefs false}
  :aliases {"checkall" ["do"
                        ["kibit"]
                        ["bikeshed"]
                        ["eastwood"]
                        ["cljfmt" "check"]]
            ;"test" ["cloverage"]
            }
  :cljfmt {:indents {#".*" [[:block 0]]}}
  :profiles {:test {:resource-paths ["resources/dev" "resources/test"]
                    :dependencies [[cheshire "5.8.1"]
                                  [ring/ring-mock "0.3.2"]
                                  [ring/ring-devel "1.7.1"
                                   :exclusions [ring/ring-core]]]}
             :dev {:main oph.ehoks.dev-server
                   :dependencies [[cheshire "5.8.1"]
                                  [ring/ring-mock "0.3.2"]
                                  [ring/ring-devel "1.7.1"
                                   :exclusions [ring/ring-core]]]
                   :resource-paths ["resources/dev" "resources/dev/src"]}
             :uberjar {:resource-paths ["resources/uberjar"]}})

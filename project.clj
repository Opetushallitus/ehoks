(defproject oph.ehoks "0.1.1"
  :description "OPH eHOKS Backend"
  :min-lein-version "2.8.1"
  :pedantic? :abort
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-http "3.10.0"]
                 [com.layerware/hugsql "0.5.1"]
                 [com.taoensso/carmine "2.19.1"]
                 [metosin/compojure-api "2.0.0-alpha28"]
                 [org.flywaydb/flyway-core "6.3.3"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.postgresql/postgresql "42.2.12"]
                 [ring/ring-jetty-adapter "1.8.0"]
                 [clj-time "0.15.2"]
                 [org.clojure/core.async "0.4.490"]
                 [org.clojure/tools.logging "1.0.0"]
                 [org.apache.logging.log4j/log4j-api "2.11.1"]
                 [org.apache.logging.log4j/log4j-core "2.11.1"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.11.1"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.json "1.0.0"]
                 [environ "1.1.0"]
                 [software.amazon.awssdk/sqs "2.5.37"]
                 [fi.vm.sade/auditlogger "8.3.0-20190605.103856-7"]]
  :managed-dependencies [[org.clojure/clojure "1.10.0"]

                         ;; http server
                         [javax.servlet/javax.servlet-api "4.0.1"]
                         [metosin/compojure-api "2.0.0-alpha26"]
                         [ring/ring-codec "1.1.2"]
                         [ring/ring-core "1.8.0"]
                         [ring/ring-jetty-adapter "1.8.0"]
                         [ring/ring-servlet "1.8.0"]

                         ;; http client
                         [clj-http "3.10.0"]
                         [org.apache.httpcomponents/httpasyncclient "4.1.4"]
                         [org.apache.httpcomponents/httpclient "4.5.12"]
                         [org.apache.httpcomponents/httpclient-cache "4.5.12"]
                         [org.apache.httpcomponents/httpcore "4.4.13"]
                         [org.apache.httpcomponents/httpcore-nio "4.4.13"]
                         [org.apache.httpcomponents/httpmime "4.5.12"]

                         ;; logging
                         [org.clojure/tools.logging "0.4.1"]
                         [org.apache.logging.log4j/log4j-api "2.11.1"]
                         [org.apache.logging.log4j/log4j-core "2.11.1"]
                         [org.apache.logging.log4j/log4j-slf4j-impl "2.11.1"]

                         ;; date, time
                         [joda-time "2.10.5"]
                         [clj-time "0.15.2"]

                         ;; json
                         [com.fasterxml.jackson.core/jackson-annotations "2.11.0.rc1"]
                         [com.fasterxml.jackson.core/jackson-core "2.11.0.rc1"]
                         [com.fasterxml.jackson.core/jackson-databind "2.11.0.rc1"]
                         [com.fasterxml.jackson.core/jackson-datatype-jsr310 "2.9.8"]
                         [org.clojure/data.json "1.0.0"]
                         [com.google.code.gson/gson "2.8.6"]

                         ;; XML
                         [org.clojure/data.xml "0.0.8"]

                         ;; postresql
                         [com.layerware/hugsql "0.5.1"]
                         [org.clojure/java.jdbc "0.7.11"]
                         [org.flywaydb/flyway-core "6.3.3"]
                         [org.postgresql/postgresql "42.2.12"]

                         ;; other
                         [org.clojure/core.async "0.4.490"]
                         [commons-codec "1.14"]
                         [commons-fileupload "1.4"]
                         [commons-io "2.6"]
                         [hiccup "1.0.5"]
                         [org.clojure/tools.namespace "0.2.11"]
                         [environ "1.1.0"]
                         [software.amazon.awssdk/sqs "2.5.37"]

                         ;; Plugins
                         [org.clojure/tools.reader "1.3.2"]
                         [io.aviso/pretty "0.1.37"]
                         [instaparse "1.4.10"]]
  :plugins [[lein-cljfmt "0.6.0" :exclusions [org.clojure/tools.cli]]
            [lein-kibit "0.1.6"]
            [lein-bikeshed "0.5.2"]
            [jonase/eastwood "0.3.1"]
            [lein-auto "0.1.3"]
            [lein-ancient "0.6.15"]
            [lein-cloverage "1.0.13"]
            [lein-eftest "0.5.7"]
            [lein-environ "1.1.0"]]
  :repositories [["releases" {:url           "https://artifactory.opintopolku.fi/artifactory/oph-sade-release-local"
                              :sign-releases false
                              :snapshots     false}]
                 ["snapshots" {:url "https://artifactory.opintopolku.fi/artifactory/oph-sade-snapshot-local"}]]
  :main oph.ehoks.main
  :aot [oph.ehoks.main]
  :uberjar-name "ehoks-standalone.jar"
  :source-paths ["src"]
  :resource-paths ["resources/prod"
                   "resources/prod/src"
                   "resources/public"
                   "resources/db"]
  :cloverage {;:fail-threshold 90
              :html? false}
  :aliases {"checkall" ["with-profile" "+test" "do"
                        ["kibit"]
                        ["bikeshed"]
                        ["eastwood"]
                        ["cljfmt" "check"]]
            "gendoc" ["do"
                      ["run" "-m" "oph.ehoks.hoks-doc/write-doc!" "doc/hoks.md"]]
            "dbmigrate" ["run" "-m" "oph.ehoks.db.migrations/migrate!"]
            "dbclean" ["run" "-m" "oph.ehoks.db.migrations/clean!"]
            "import" ["run" "-m" "oph.ehoks.import/lein-import-file!"]
            "genmigration" ["run" "-m" "oph.ehoks.migration-tools/lein-genmigration"]}
  :cljfmt {:indents {#".*" [[:block 0]]}}
  :profiles {:test {:resource-paths ["resources/test"
                                     "resources/test/src"
                                     "resources/test/config"]
                    :dependencies [[cheshire "5.10.0"]
                                   [ring/ring-mock "0.4.0"]
                                   [ring/ring-devel "1.8.0"
                                    :exclusions [ring/ring-core]]]
                    :env {:config "oph-configuration/test.edn"}}
             :dev {:main oph.ehoks.dev-server
                   :dependencies [[cheshire "5.10.0"]
                                  [ring/ring-mock "0.4.0"]
                                  [ring/ring-devel "1.8.0"
                                   :exclusions [ring/ring-core]]
                                  [camel-snake-kebab "0.4.1"]]
                   :resource-paths ["resources/dev"
                                    "resources/test/src"
                                    "resources/dev/src"
                                    "resources/prod/src"]}
             :uberjar {:resource-paths ["resources/uberjar"
                                        "resources/public"]}})

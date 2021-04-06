(defproject oph.ehoks "0.1.1"
  :description "OPH eHOKS Backend"
  :min-lein-version "2.8.1"
  :pedantic? :abort
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [clj-http "3.12.1"]
                 [com.layerware/hugsql "0.5.1"]
                 [com.taoensso/carmine "3.1.0"]
                 [metosin/compojure-api "2.0.0-alpha28"]
                 [org.flywaydb/flyway-core "7.7.2"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [org.postgresql/postgresql "42.2.19"]
                 [ring/ring-jetty-adapter "1.9.2"]
                 [clj-time "0.15.2"]
                 [org.clojure/core.async "1.3.610"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.apache.logging.log4j/log4j-api "2.14.1"]
                 [org.apache.logging.log4j/log4j-core "2.14.1"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.14.1"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.json "2.0.2"]
                 [environ "1.2.0"]
                 [software.amazon.awssdk/sqs "2.16.35"]
                 [fi.vm.sade/auditlogger "8.3.0-20190605.103856-7"]
                 [com.rpl/specter "1.1.3"]
                 [cheshire "5.10.0"]]
  :managed-dependencies [[org.clojure/clojure "1.10.3"]

                         ;; http server
                         [javax.servlet/javax.servlet-api "4.0.1"]
                         [metosin/compojure-api "2.0.0-alpha26"]
                         [ring/ring-codec "1.1.3"]
                         [ring/ring-core "1.9.2"]
                         [ring/ring-jetty-adapter "1.9.2"]
                         [ring/ring-servlet "1.9.2"]

                         ;; http client
                         [clj-http "3.12.1"]
                         [org.apache.httpcomponents/httpasyncclient "4.1.4"]
                         [org.apache.httpcomponents/httpclient "4.5.13"]
                         [org.apache.httpcomponents/httpclient-cache "4.5.13"]
                         [org.apache.httpcomponents/httpcore "4.4.14"]
                         [org.apache.httpcomponents/httpcore-nio "4.4.14"]
                         [org.apache.httpcomponents/httpmime "4.5.13"]

                         ;; logging
                         [org.clojure/tools.logging "1.1.0"]
                         [org.apache.logging.log4j/log4j-api "2.14.1"]
                         [org.apache.logging.log4j/log4j-core "2.14.1"]
                         [org.apache.logging.log4j/log4j-slf4j-impl "2.14.1"]
                         [org.slf4j/slf4j-api "1.7.28"]

                         ;; date, time
                         [joda-time "2.10.10"]
                         [clj-time "0.15.2"]

                         ;; json
                         [com.fasterxml.jackson.core/jackson-annotations "2.12.2"]
                         [com.fasterxml.jackson.core/jackson-core "2.12.2"]
                         [com.fasterxml.jackson.core/jackson-databind "2.12.2"]
                         [com.fasterxml.jackson.core/jackson-datatype-jsr310 "2.9.8"]
                         [org.clojure/data.json "2.0.2"]
                         [com.google.code.gson/gson "2.8.6"]
                         [cheshire "5.10.0"]

                         ;; XML
                         [org.clojure/data.xml "0.0.8"]

                         ;; postresql
                         [com.layerware/hugsql "0.5.1"]
                         [org.clojure/java.jdbc "0.7.12"]
                         [org.flywaydb/flyway-core "7.7.2"]
                         [org.postgresql/postgresql "42.2.19"]

                         ;; other
                         [org.clojure/core.async "1.3.610"]
                         [commons-codec "1.15"]
                         [commons-fileupload "1.4"]
                         [commons-io "2.8.0"]
                         [hiccup "1.0.5"]
                         [org.clojure/tools.namespace "1.1.0"]
                         [environ "1.2.0"]
                         [software.amazon.awssdk/sqs "2.16.35"]
                         [org.reactivestreams/reactive-streams "1.0.3"]
                         [org.clojure/java.classpath "1.0.0"]

                         ;; Plugins
                         [org.clojure/tools.reader "1.3.5"]
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
                                   [ring/ring-devel "1.9.2"
                                    :exclusions [ring/ring-core]]]
                    :env {:config "oph-configuration/test.edn"}}
             :dev {:main oph.ehoks.dev-server
                   :dependencies [[cheshire "5.10.0"]
                                  [ring/ring-mock "0.4.0"]
                                  [ring/ring-devel "1.9.2"
                                   :exclusions [ring/ring-core]]
                                  [camel-snake-kebab "0.4.2"]]
                   :resource-paths ["resources/dev"
                                    "resources/test/src"
                                    "resources/dev/src"
                                    "resources/prod/src"]}
             :uberjar {:resource-paths ["resources/uberjar"
                                        "resources/public"]}})

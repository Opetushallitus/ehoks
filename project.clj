(defproject oph.ehoks "0.1.1"
  :description "OPH eHOKS Backend"
  :min-lein-version "2.8.1"
  :pedantic? :abort
  :dependencies [[org.clojure/clojure]
                 [clj-http]
                 [cheshire]
                 [com.googlecode.libphonenumber/libphonenumber]
                 [metosin/compojure-api]
                 [org.flywaydb/flyway-core]
                 [org.clojure/java.jdbc]
                 [org.postgresql/postgresql]
                 [ring/ring-jetty-adapter]
                 [clj-time]
                 [org.clojure/core.async]
                 [org.clojure/tools.logging]
                 [org.apache.logging.log4j/log4j-api]
                 [org.apache.logging.log4j/log4j-core]
                 [org.apache.logging.log4j/log4j-slf4j-impl]
                 [org.clojure/data.xml]
                 [org.clojure/data.json]
                 [environ]
                 [software.amazon.awssdk/sqs]
                 [fi.vm.sade/auditlogger]
                 [com.rpl/specter]
                 [org.clojure/core.memoize]]
  :managed-dependencies [[org.clojure/clojure "1.11.1"]

                         ;; http server
                         [javax.servlet/javax.servlet-api "4.0.1"]
                         [metosin/compojure-api "2.0.0-alpha31"]
                         [ring/ring-codec "1.2.0"]
                         [ring/ring-core "1.10.0"]
                         [ring/ring-jetty-adapter "1.10.0"]
                         [ring/ring-servlet "1.10.0"]

                         ;; http client
                         [clj-http "3.12.3"]
                         [org.apache.httpcomponents/httpasyncclient "4.1.5"]
                         [org.apache.httpcomponents/httpclient "4.5.14"]
                         [org.apache.httpcomponents/httpclient-cache "4.5.14"]
                         [org.apache.httpcomponents/httpcore "4.4.16"]
                         [org.apache.httpcomponents/httpcore-nio "4.4.16"]
                         [org.apache.httpcomponents/httpmime "4.5.14"]
                         [cheshire "5.12.0"]

                         ;; logging
                         [org.clojure/tools.logging "1.2.4"]
                         [org.apache.logging.log4j/log4j-api "2.22.0"]
                         [org.apache.logging.log4j/log4j-core "2.22.0"]
                         [org.apache.logging.log4j/log4j-slf4j-impl "2.22.0"]

                         ;; date, time
                         [joda-time "2.12.5"]
                         [clj-time "0.15.2"]

                         ;; json
                         [com.fasterxml.jackson.core/jackson-annotations "2.16.0"]
                         [com.fasterxml.jackson.core/jackson-core "2.16.0"]
                         [com.fasterxml.jackson.core/jackson-databind "2.16.0"]
                         [com.fasterxml.jackson.core/jackson-datatype-jsr310 "2.16.0"]
                         [org.clojure/data.json "2.4.0"]
                         [com.google.code.gson/gson "2.10.1"]
                         [com.tananaev/json-patch "1.2"]

                         ;; XML
                         [org.clojure/data.xml "0.0.8"]

                         ;; postgresql
                         [org.clojure/java.jdbc "0.7.12"]
                         [org.flywaydb/flyway-core "6.5.7"]
                         [org.postgresql/postgresql "42.7.1"]

                         ;; other
                         [org.clojure/core.async "1.6.681"]
                         [org.clojure/core.memoize "1.0.257"]
                         [commons-codec "1.16.0"]
                         [commons-fileupload "1.5"]
                         [commons-io "2.15.1"]
                         [hiccup "1.0.5"]
                         [org.clojure/tools.namespace "1.4.4"]
                         [environ "1.2.0"]
                         [software.amazon.awssdk/sqs "2.21.40" :exclusions [org.slf4j/slf4j-api]]
                         [com.googlecode.libphonenumber/libphonenumber "8.13.26"]
                         [fi.vm.sade/auditlogger "9.2.2-20231114.085926-4"]
                         [com.rpl/specter "1.1.4"]

                         ;; Plugins
                         [org.clojure/tools.reader "1.3.7"]
                         [io.aviso/pretty "1.4.4"]
                         [instaparse "1.4.12"]]
  :plugins [[lein-cljfmt "0.6.6" :exclusions [org.clojure/tools.cli]]
            [lein-kibit "0.1.8" :exclusions [org.clojure/clojure]]
            [lein-bikeshed "0.5.2"]
            [jonase/eastwood "1.4.2"]
            [lein-auto "0.1.3"]
            [lein-ancient "0.7.0"]
            [lein-cloverage "1.2.4"]
            [lein-eftest "0.6.0"]
            [lein-environ "1.2.0"]]
  :repositories [["github" {:url "https://maven.pkg.github.com/Opetushallitus/packages"
                            :username "private-token"
                            :password :env/GITHUB_TOKEN}]
                 ["oph-releases" "https://artifactory.opintopolku.fi/artifactory/oph-sade-release-local"]
                 ["oph-snapshots" "https://artifactory.opintopolku.fi/artifactory/oph-sade-snapshot-local"]
                 ["ext-snapshots" "https://artifactory.opintopolku.fi/artifactory/ext-snapshot-local"]]
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
  :eastwood {}
  :profiles {:test {:resource-paths ["resources/test"
                                     "resources/test/src"
                                     "resources/test/config"]
                    :dependencies [[cheshire "5.12.0"]
                                   [ring/ring-mock "0.4.0"]
                                   [ring/ring-devel "1.10.0"
                                    :exclusions [ring/ring-core
                                                 org.clojure/java.classpath]]]
                    :env {:config "oph-configuration/test.edn"}}
             :schemaspy {:dependencies [[net.sourceforge.schemaspy/schemaspy "5.0.0"]]}
             :dev {:main oph.ehoks.dev-server
                   :dependencies [[cheshire "5.12.0"]
                                  [ring/ring-mock "0.4.0"]
                                  [ring/ring-devel "1.10.0"
                                   :exclusions [ring/ring-core
                                                org.clojure/java.classpath]]
                                  [camel-snake-kebab "0.4.3"]]
                   :resource-paths ["resources/dev"
                                    "resources/test/src"
                                    "resources/dev/src"
                                    "resources/prod/src"]}
             :uberjar {:resource-paths ["resources/uberjar"
                                        "resources/public"]}})

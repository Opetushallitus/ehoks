(defproject oph.ehoks "0.1.1"
  :description "OPH eHOKS Backend"
  :min-lein-version "2.8.1"
  :pedantic? :abort
  :dependencies [[org.clojure/clojure]
                 [clj-http]
                 [clj-tuple]
                 [cheshire]
                 [com.googlecode.libphonenumber/libphonenumber]
                 [dev.weavejester/medley]
                 [com.taoensso/faraday]
                 [metosin/compojure-api]
                 [org.flywaydb/flyway-core]
                 [org.clojure/java.jdbc]
                 [org.postgresql/postgresql]
                 [com.layerware/hugsql]
                 [ring/ring-jetty-adapter]
                 [clj-time]
                 [jarohen/chime]
                 [org.clojure/core.async]
                 [org.clojure/tools.logging]
                 [org.apache.logging.log4j/log4j-api]
                 [org.apache.logging.log4j/log4j-core]
                 [org.apache.logging.log4j/log4j-slf4j-impl]
                 [org.clojure/data.xml]
                 [org.clojure/data.json]
                 [environ]
                 [software.amazon.awssdk/sqs]
                 [com.rpl/specter]
                 [org.clojure/core.memoize]]
  :managed-dependencies [[org.clojure/clojure "1.12.0"]
                         [clj-tuple "0.2.2"]

                         ;; http server
                         [javax.servlet/javax.servlet-api "4.0.1"]
                         [metosin/compojure-api "2.0.0-alpha31" :exclusions [medley]]
                         [ring/ring-codec "1.2.0"]
                         [ring/ring-core "1.13.0"]
                         [ring/ring-jetty-adapter "1.13.0" :exclusions [org.slf4j/slf4j-api]]
                         [ring/ring-servlet "1.13.0"]

                         ;; http client
                         [clj-http "3.13.0"]
                         [org.apache.httpcomponents/httpasyncclient "4.1.5"]
                         [org.apache.httpcomponents/httpclient "4.5.14"]
                         [org.apache.httpcomponents/httpclient-cache "4.5.14"]
                         [org.apache.httpcomponents/httpcore "4.4.16"]
                         [org.apache.httpcomponents/httpcore-nio "4.4.16"]
                         [org.apache.httpcomponents/httpmime "4.5.14"]
                         [cheshire "5.13.0"]

                         ;; logging
                         [org.clojure/tools.logging "1.3.0"]
                         [org.apache.logging.log4j/log4j-api "2.24.1"]
                         [org.apache.logging.log4j/log4j-core "2.24.1"]
                         ; pipes ring/jetty logging (slf4j) to log4j2
                         [org.apache.logging.log4j/log4j-slf4j-impl "2.24.1"]

                         ;; date, time
                         [joda-time "2.13.0"]
                         [clj-time "0.15.2"]
                         [jarohen/chime "0.3.3"]

                         ;; json
                         [com.fasterxml.jackson.core/jackson-annotations "2.18.1"]
                         [com.fasterxml.jackson.core/jackson-core "2.18.1"]
                         [com.fasterxml.jackson.core/jackson-databind "2.18.1"]
                         [com.fasterxml.jackson.core/jackson-datatype-jsr310 "2.18.1"]
                         [org.clojure/data.json "2.5.0"]
                         [com.google.code.gson/gson "2.11.0"]

                         ;; XML
                         [org.clojure/data.xml "0.0.8"]

                         ;; postgresql
                         [org.clojure/java.jdbc "0.7.12"]
                         [org.flywaydb/flyway-core "6.5.7"]
                         [org.postgresql/postgresql "42.7.4"]
                         [com.layerware/hugsql "0.5.3"]

                         ;; other
                         [dev.weavejester/medley "1.8.1"]
                         [com.taoensso/faraday "1.12.3" ]
                         [org.clojure/core.async "1.6.681"]
                         [org.clojure/core.memoize "1.1.266"]
                         [commons-codec "1.17.1"]
                         [commons-fileupload "1.5"]
                         [commons-io "2.17.0"]
                         [hiccup "1.0.5"]
                         [org.clojure/tools.namespace "1.5.0"]
                         [environ "1.2.0"]
                         [software.amazon.awssdk/sqs "2.29.4" :exclusions [org.slf4j/slf4j-api]]
                         [com.googlecode.libphonenumber/libphonenumber "8.13.49"]
                         [com.rpl/specter "1.1.4"]
                         [ring/ring-mock "0.4.0"]
                         [ring/ring-devel "1.13.0" :exclusions [ring/ring-core
                                                                org.clojure/java.classpath]]
                         [camel-snake-kebab "0.4.3"]

                         ;; Plugins
                         [org.clojure/tools.reader "1.5.0"]
                         [io.aviso/pretty "1.4.4"]
                         [instaparse "1.5.0"]]
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
  :profiles {:test {:resource-paths ["resources/test" "resources/test/src"]
                    :dependencies [[ring/ring-mock]
                                   [ring/ring-devel]]
                    :env {:config "oph-configuration/test.edn"
                          :aws-region "eu-west-1"
                          :aws-endpoint-url "http://localhost:18000"}}
             :schemaspy {:dependencies [[net.sourceforge.schemaspy/schemaspy "5.0.0"]]}
             :dev {:main oph.ehoks.dev-server
                   :dependencies [[ring/ring-mock]
                                  [ring/ring-devel]
                                  [camel-snake-kebab]]
                   :env {:config "oph-configuration/dev.edn"}
                   :resource-paths ["resources/dev"
                                    "resources/test/src"
                                    "resources/dev/src"
                                    "resources/prod/src"]}
             :uberjar {:resource-paths ["resources/uberjar"
                                        "resources/public"]}})

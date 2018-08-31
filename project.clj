(defproject oph.ehoks "0.1.0"
  :description "OPH eHOKS Backend"
  :min-lein-version "2.8.1"
  :pedantic? :abort
  :dependencies [[org.clojure/clojure "1.9.0"]
                 ; Force dependency versions of dependenciens for avoiding
                 ; confusing overrides
                 [joda-time "2.10"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.clojure/tools.cli "0.3.7"]
                 [ring/ring-codec "1.1.1"]
                 [commons-codec "1.11"]
                 [clj-time "0.14.4"]
                 [commons-fileupload "1.3.3"]
                 [commons-io "2.6"]
                 [ring/ring-servlet "1.6.3"]
                 [ring/ring-core "1.6.3"]
                 [com.fasterxml.jackson.core/jackson-core "2.9.6"]

                 [metosin/compojure-api "2.0.0-alpha23"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [org.flywaydb/flyway-core "5.1.4"]
                 [org.clojure/java.jdbc "0.7.8"]
                 [org.postgresql/postgresql "42.2.5"]
                 [com.layerware/hugsql "0.4.9"]
                 [environ "1.1.0"]
                 [clj-http "3.9.1"]
                 [com.taoensso/carmine "2.18.1"]
                 [hiccup "1.0.5"]]
  :plugins [[lein-ring "0.12.4"]
            [lein-cljfmt "0.6.0" :exclusions [org.clojure/tools.cli]]
            [lein-kibit "0.1.6"]
            [lein-bikeshed "0.5.1"]
            [jonase/eastwood "0.2.9"]
            [lein-auto "0.1.3"]
            [lein-ancient "0.6.15"]]
  :ring {:handler oph.ehoks.handler/app}
  :uberjar-name "ehoks.jar"
  :source-paths ["src"]
  :aliases {"checkall" ["do"
                        ["kibit"]
                        ["bikeshed"]
                        ["eastwood"]
                        ["cljfmt" "check"]]}
  :cljfmt {:indents {#".*" [[:block 0]]}}
  :profiles {:dev {:dependencies
                   [[javax.servlet/javax.servlet-api "4.0.1"]
                    [cheshire "5.8.0"]
                    [ring/ring-mock "0.3.2"]
                    [ring/ring-devel "1.7.0-RC2" :exclusions [ring/ring-core]]]
                   :resource-paths ["resources/dev" "resources/dev/src"]
                   :ring {:handler oph.ehoks.dev-server/dev-app}}})

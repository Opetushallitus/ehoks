(defproject oph.ehoks "0.1.0"
  :description "OPH eHOKS Backend"
  :min-lein-version "2.8.1"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [metosin/compojure-api "2.0.0-alpha23"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [org.flywaydb/flyway-core "5.1.4"]
                 [org.clojure/java.jdbc "0.7.6"]
                 [org.postgresql/postgresql "42.2.2"]
                 [com.layerware/hugsql "0.4.9"]
                 [environ "1.1.0"]
                 [clj-http "3.9.1"]]
  :plugins [[lein-ring "0.12.4"]
            [lein-cljfmt "0.6.0"]
            [lein-kibit "0.1.6"]
            [lein-bikeshed "0.5.1"]
            [jonase/eastwood "0.2.9"]
            [lein-environ "1.1.0"]]
  :ring {:handler oph.ehoks.handler/app}
  :uberjar-name "ehoks.jar"
  :source-paths ["src"]
  :aliases {"checkall" ["do"
                        ["kibit"]
                        ["bikeshed"]
                        ["eastwood"]
                        ["cljfmt" "check"]]}
  :cljfmt {:indents {#".*" [[:block 0]]}}
  :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]
                                  [cheshire "5.8.0"]
                                  [ring/ring-mock "0.3.2"]
                                  [ring/ring-devel "1.7.0-RC1"]]
                   :resource-paths ["resources/dev"]
                   :ring {:handler oph.ehoks.dev-server/dev-app}}})

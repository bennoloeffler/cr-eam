(defproject cr-eam "0.1.0-SNAPSHOT"
  :description "Easy CRM based on Evernote."
  :url "https://github.com/bennoloeffler/cr-eam"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-core "1.9.6"]
                 [ring/ring-jetty-adapter "1.9.6"]
                 [compojure "1.7.0"]]
  :min-lein-version "2.0.0"
  :main ^:skip-aot cr-eam.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true" "-Dorg.eclipse.jetty.util.log.announce=false"]
                       :uberjar-name "cr-eam.jar"}})

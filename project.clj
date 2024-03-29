(defproject cr-eam "0.1.0-SNAPSHOT"
  :description "Easy CRM based on Evernote."
  :url "https://github.com/bennoloeffler/cr-eam"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]

                 [com.taoensso/timbre "6.0.1"]

                 ;; route slf4j through timbre
                 [com.fzakaria/slf4j-timbre "0.3.21"]

                 ; route everything through slf4j
                 ;[org.slf4j/slf4j-api "2.0.3"]
                 [org.clojure/tools.logging "1.2.4"]
                 [org.slf4j/log4j-over-slf4j "2.0.3"]
                 [org.slf4j/jul-to-slf4j "2.0.3"]
                 [org.slf4j/jcl-over-slf4j "2.0.3"]

                 [ring/ring-core "1.9.6"]
                 [ring/ring-jetty-adapter "1.9.6"]
                 [compojure "1.7.0"]
                 [hiccup "1.0.5"]
                 [mvxcvi/puget "1.3.4"]
                 [cprop "0.1.19"]
                 [io.replikativ/konserve "0.7.294"]
                 [io.replikativ/datahike "0.6.1531"]
                 ;[io.replikativ/datahike "0.5.1517"]
                 ;[io.replikativ/datahike-postgres "0.1.0"]]
                 [io.replikativ/datahike-jdbc "0.1.2-SNAPSHOT"]

                 [org.postgresql/postgresql "42.5.0"]

                 [re-frame "1.2.0"]
                 [reagent "1.1.1"]
                 [thheller/shadow-cljs "2.20.3" :scope "provided"]
                 [org.clojure/clojurescript "1.11.60" :scope "provided"]
                 [com.google.javascript/closure-compiler-unshaded "v20220803"]]


  :min-lein-version "2.0.0"
  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :test-paths ["test/clj"]
  :resource-paths ["resources" "target/cljsbuild"]

  :main ^:skip-aot cr-eam.core
  :plugins []
  :clean-targets ^{:protect false} [:target-path "target/cljsbuild"]

  :target-path "target/%s/"
  :profiles {:uberjar {:omit-source true

                       :prep-tasks ["compile" ["run" "-m" "shadow.cljs.devtools.cli" "release" "app"]]
                       :aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true" "-Dorg.eclipse.jetty.util.log.announce=false"]
                       :uberjar-name "cr-eam.jar"}
             :project/dev {:dependencies [[re-frisk "1.6.0"]]}})

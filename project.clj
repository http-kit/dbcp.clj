(defproject http-kit/dbcp "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :warn-on-reflection true
  :java-source-path "src/java"
  :jar-exclusions [#".*java$"]
  :javac-options {:source "1.6" :target "1.6" :debug "true" :fork "true"}
  :plugins [[lein-swank "1.4.4"]]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/java.jdbc "0.2.3"]]
  :profiles {:dev {:dependencies [[mysql/mysql-connector-java "5.1.21"]
                                  [commons-dbcp "1.4"]
                                  [junit/junit "4.8.2"]]}})

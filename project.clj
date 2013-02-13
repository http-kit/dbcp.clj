(defproject http-kit/dbcp "0.1.0"
  :description "High performance database connection pool for Clojure"
  :url "https://github.com/http-kit/dbcp.clj"
  :warn-on-reflection true
  :java-source-path "src/java"
  :jar-exclusions [#".*java$"]
  :javac-options {:source "1.6" :target "1.6" :debug "true" :fork "true"}
  :plugins [[lein-swank "1.4.4"]]
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/java.jdbc "0.2.3"]]
  :profiles {:dev {:dependencies [[mysql/mysql-connector-java "5.1.21"]
                                  [commons-dbcp "1.4"]
                                  [junit/junit "4.8.2"]]}})

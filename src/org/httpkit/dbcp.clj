(ns org.httpkit.dbcp
  (:import org.httpkit.dbcp.PerThreadDataSource)
  (:require [clojure.java.jdbc :as jdbc]))

(defonce db-factory (atom {}))

(defn close-database! []
  (if-let [ds (:ds @db-factory)]
    (.close ^PerThreadDataSource ds)
    (reset! db-factory nil)))

(defn use-database!
  "jdbcurl: jdbc:mysql://localhost/test"
  [jdbcurl user pass]
  (let [ds (PerThreadDataSource. jdbcurl user pass)]
    (reset! db-factory {:factory (fn [& args] (.getConnection ds))
                        :ds ds})))

(defmacro with-db [& body]
  `(jdbc/with-connection @db-factory
     ~@body))

(defn do-commands [& commands]
  (jdbc/with-connection @db-factory
    (apply jdbc/do-commands commands)))

(defn query [& query]
  (with-db (jdbc/with-query-results rs (vec query) (doall rs))))

(defn update-values [table where-params record]
  (with-db (jdbc/update-values table where-params record)))

(defn delete-rows [table where-params]
  (with-db (jdbc/delete-rows table where-params)))

(defn insert-record [table record]
  (with-db (jdbc/insert-record table record)))

(defn insert-records [& args]
  (with-db (apply jdbc/insert-records args)))

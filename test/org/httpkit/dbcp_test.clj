(ns org.httpkit.dbcp-test
  (:import java.sql.SQLException)
  (:use clojure.test
        [clojure.java.jdbc :only [print-sql-exception-chain]]
        org.httpkit.dbcp))

(defn mysql-fixture [test-fn]
  (let [test-db-name "dbcp_test"]
    (try
      (use-database! "jdbc:mysql://localhost/test" "root" "")
      (do-commands (str "DROP DATABASE IF EXISTS " test-db-name))
      (do-commands (str "CREATE DATABASE " test-db-name))
      (use-database! (str "jdbc:mysql://localhost/" test-db-name) "root" "")

      (do-commands "CREATE TABLE test_table (
                        id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
                        test_key VARCHAR(20),
                        test_value VARCHAR(100))")
      (test-fn)
      (do-commands (str "DROP DATABASE " test-db-name))
      (catch SQLException e
        (print-sql-exception-chain e)
        (throw e)))))

(use-fixtures :each mysql-fixture)

(deftest test-dbcp
  (let [total 10
        inserted (doall (map (fn [id] (insert-record :test_table
                                                    {:test_key (str "key_" id)
                                                     :test_value (str "value_" id)}))
                             (range 0 total)))
        selected (query "select * from test_table where id < ?" 8)]
    (doseq [t inserted]
      (is (:generated_key t)))
    (is (= 7 (count selected)))
    (is (= 10 (count (query "select * from test_table"))))
    (delete-rows :test_table ["id=?" 7])
    (is (= 9 (count (query "select * from test_table"))))
    (insert-records :test_table
                    {:test_key "key1"
                     :test_value "value"}
                    {:test_key "key2"
                     :test_value "value"})
    (is (= 11 (count (query "select * from test_table"))))
    (let [v "this is a test"
          id 4]
      (update-values :test_table ["id=?" id] {:test_value v})
      (is (= v (-> (query "select * from test_table where id = ?" id)
                   first :test_value))))))
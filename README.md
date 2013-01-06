# Simple Database Connection pool

A jdbc connection pool. By using ThreadLocals, per connection per Thread. The connection get atomatically closed when thread died.

It's very fast, should be faster than the common object pooling way

## Why

* I write it for [Rssminer](http://rssminer.net). Rssminer needs to be fast. It use a fix number of Thread, connection
  per thread sounds reasonable.
* Code uses memory, so write less code.
* Simple and reliable
* Zero dependency

## Java Usage

```xml
<!-- pom.xml -->

<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>

<dependency>
    <groupId>me.shenfeng</groupId>
    <artifactId>dbcp</artifactId>
    <version>1.0</version>
</dependency>

```

```java

DataSource dataSource = new PerThreadDataSource("jdbc:mysql://localhost/test", "root", "pass");
Connection con = dataSource.getConnection();
Statement stat = con.createStatement();
ResultSet rs = stat.executeQuery("select 1");

```

## Clojure Usage

```clj
;;; project.clj
:dependencies [[org.clojure/clojure "1.4.0"]
               [org.clojure/java.jdbc "0.2.3"]
               [me.shenfeng/dbcp "1.0"]]
(ns you-ns
  (:import me.shenfeng.dbcp.PerThreadDataSource)
  (:use [clojure.java.jdbc :only [with-connection do-prepared
                                  with-query-results insert-record]]))
(defonce mysql-factory (atom nil))
(defn use-mysql-database! [jdbcurl user pass]
  (let [ds (PerThreadDataSource. jdbcurl user pass)]
    (reset! mysql-factory {:factory (fn [& args] (.getConnection ds))
                           :ds ds})))
(defn close-mysql-factory! []
  (when-let [ds (:ds @mysql-factory)]
    (.close ^PerThreadDataSource ds)
    (reset! mysql-factory nil)))

(defmacro with-mysql [& body]
  `(with-connection @mysql-factory
     ~@body))
;;; example
(with-mysql
  (do-prepared "UPDATE users SET name = ? WHERE id = ?" [name id]))

(defn mysql-query [query]
  (with-mysql (with-query-results rs query (doall rs))))
;;; example
(mysql-query ["SELECT * FROM users WHERE id = ?" 1]) ; return a seq of map
=> ({:name "name"
     :id 1
     :email "email@example.com"
     ...})

(defn mysql-insert [table record]
  (:generated_key (with-mysql (insert-record table record))))
;;; example
(mysql-insert :my_table {:name "" :key "value"}) ; return generated id

```

## Feature
* Clean and compact code.
* Reconnect if closed

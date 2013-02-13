# Database Connection pool for Clojure

A jdbc connection pool written from scratch, using ThreadLocals, per Thread per connection. Works very good with http-kit server's thread pool model

Current semantic version: `[http-kit/dbcp "0.1.0"]`.

## wait, Is the API blocking (sync)? blocking = slow

Of course blocking! Blocking API is much much easier to use.

> blocking = slow?

If you only have one thread, that's may be the case, you can do nothing but waiting for server if blocking.
For Clojure, not the case, Clojure has multithreading, you can accept incoming request and wait for MySQL at the same time!

> A SQL query can run more than 10s

It's time to double check your data and SQL statement. Non-blocking does not save you!

## Feature
* Clean and compact code: jar size is about ~11k
* Fast, very fast.
* The connection get atomatically closed when thread died.
* Reconnect if closed

## Usage

```clj
(:require [org.httpkit.dbcp :as db])

(db/use-database! "jdbc:mysql://localhost/test" "user" "password")

;; insert value
(db/insert-record :your_table {:column1 "value1"
                               :column2 "value2"
                               :column3 2})
=> {:generated_key 1} ;; mysql

;; update
(db/update-values :your_table ["column1 = ?" "value1"]
                  {:column1 "new-value"
                   :column3 1000})

;;query
(db/query "select * from your_table where column1 = ?" "value1")
=> ({:id 1 :column1 "new-value" :column2 "vlaue2" :column3 1000})

;; delete
(db/delete-rows :your_table ["column1 = ?" "value1"])

(db/close-database!)

```

### Contact

Please use the [GitHub issues page](https://github.com/http-kit/dbcp.clj/issues) for feature suggestions, bug reports, or general discussions.

### License

Copyright &copy; 2012 [Feng Shen](http://shenfeng.me/). Distributed under the [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

# Simple Database Connection pool

* A `javax.sql.DataSource` written for
  [Rssminer](http://rssminer.net). by using
  `java.lang.ThreadLocal`. Per thread per connection.

## Why

* Rssminer needs to be fast. It use a fix number of Thread, connection
  per thread sounds reasonable, should be faster than common way.
* Code use memory, so write less code.


## Feature
* Clean and compact code.
* Reconnect if closed

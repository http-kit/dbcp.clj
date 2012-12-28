# Simple Database Connection pool

A jdbc connection pool. By using ThreadLocals, per connection per Thread.

The connection get atomatically closed when thread died, so, there is no connection leak

Very fast.  Should be faster than the common object pooling way

## Why

* I write it for [Rssminer](http://rssminer.net), it needs to be fast. It use a fix number of Thread, connection
  per thread sounds reasonable.
* Code uses memory, so write less code.


## Feature
* Clean and compact code.
* Reconnect if closed

# Ideas: Sleeves REST API

## Background

Persistent, replicated message buses have many other uses than "big data" or "fast data". They can essentially change the way we create nets of microservices, providing asynchronous interconnections between the systems.

This fits well with other trends in how we re-learn to program 
computers: data binding within front end applications and immutability / functional programming at large. Persistent, replicated message buses bring all this to the last realm - the interconnections.

## State in 2016

At least two systems fit the bill: Kafka and DistributedLog. Also most/all databases can be used as immutable data queues if needed, but let's ignore that since it's not their primary purpose.

Kafka is great, and easy to get started with. However, its partitioning mechanism seems to be a bit of a pain.

DistributedLog pitches to do better where Kafka falls short: to act well under "let's rewind the whole data" scenarios and allowing more storage space to be added simply by adding more BookKeeper nodes (hey, no partitions needed!).

DistributedLog lacks in ease of getting started, quality of documentation and generally "packaging".

## What if..?

We could make a REST API (including use of Websockets) for handling such persistent data queues. We could make it so that it's independent of the underlying mechanism (it's just a proxy). We should incorporate access control to its interface. 

How light could we do this?

## API

### Access Scopes

|Scope|provides|
|---|---|
|something.*:create|Allows creation of namespaces within the given pattern|
|something.*:read|Allows reading from such a namespace|
|something.*:write|Allows writing to such a namespace|

The scopes intentionally omit any deletion capabilities. We're seeing the message bus as immutable, except for deprecation time / size limit (optional).

Traditional admin directly on the underlying cluster can be used to completely remove namespaces etc. This would be with similar rights as creating new scopes for the sleeves API.

It's really very similar to any Unix file system.. Except that all files would be append-only, and consist of records. Maybe we can use some existing model (e.g. how does HDFS do this?).

### Creation

```
POST /path[/...]/[log]

- The path could be the namespace, logs the leafs
- Creates either a namespace (folder) or a stream (log)
- Creating a deep namespace also causes all intermediate levels to be created (like with folders in a file system)
```

### Writing

```
PATCH /path[/...]/log

- Adds the body to the leaf log
- multi-form MIME type can be used for appending multiple entries
- Should there we a way to open a websocket for writing to a log? Maybe only go with websocket?
  - Is there a standard way to indicate whether we want to open a "read stream" or "write stream" to `wss://host:port/path[/...]/log`?
  - We could simply use `?read` and `?write` URL parameters
```

Open WebSocket connection to `wss://host:port/path[/...]/log?write`.

- author is provided by authentication scope `uid`
- in: original timestamp, payload
- out: (do we need something?)

It's not a good idea to allow the origin to give an offset: we want to allow for multiple simultaneous writers and things like distributedlog already take care of this.

Is there a reason that something should be returned to the writer?

Could the writer request flushing by some special code in input? Then we'd make sure all entries from it so far have reached quorum, and return 'true' once they have.

### Reading

Open WebSocket connection to `wss://host:port/path[/...]/log`. By default a connection would be read.

By default new entries to the log would be sent.

Optional Parameters:

|parameter|purpose|
|---|---|
|`since=<iso8601>`|Start reading from the provided time (inclusive)|
|`offset=<long>`|Start reading from the provided offset (inclusive)|

The offset of each record needs to be transmitted with the record, essentially making the communication a tuple. Maybe also other data about each record:

- timestamp
- author

Again, we are close to what Unix file systems provide for individual files. One (local, non-replicated) implementation of the API could be using folders as logs, and storing records by their offset number as files.

### Checking for existence

It would be nice to be able to just see if a path/log exists.

This is similar to the creation, only that if the path/log does not exist, nothing gets created.

```
GET /path[/...]/[log]
```

For paths, could list the name of logs within that path (and possible subpaths). Otherwise, 404 or 403.

For logs, could list the number of log entries, or the offset, time, author tuplets of all.

If the caller only wants to know whether something exists, it can use `HEAD` instead of `GET`.


### Listening for new entries

Open WebSocket connection to `wss://host:port/path[/...]/`.

This could be interpreted in two ways:

- tell me about any logs within this path that get new entries to them (might be useful)
- tell me about new logs being created within this path

What should we do?

We could use WebSocket connection to `wss://host:port/path[/...]/[pattern]` (`pattern` being the normal file system glob pattern) for indicating "read many logs", including new ones that would match the pattern.

That would leave the non-pattern version for getting a stream of new logs.

in: (nothing)
out: author, timestamp, log name


### Changing retention time (or size)

Retention time (or size) changes can be regarded as admin things, at least in the beginning.


### Using as (compacted) key/value store

Kafka has a "compaction" mode that allows entries in the queue that get later overwritten (based on their key value) to be skipped, thus allowing the queue to be seen as a key/value store.

This is rather useful for e.g. handling configuration, even if the underlying queue were not physically reduced in size.

However, it can also be built on top of any basic queue system, probably with more features (cascading configuration, Typesafe Config style), and it would simply complicate the inner working of the API. The cost would be unneeded traffic, but such a config can also be made to run as a separate proxy on the same machine / near the queue API, allowing separation but good performance.

So, key/value store is to be outside the focus of this API.

## Why (three) sleeves?

It's a reference to [Very Secret Service](https://en.wikipedia.org/wiki/A_Very_Secret_Service) episode where an agent finds a bathrobe in Eastern Germany with three sleeves. That's redundance and replication, and sleeves are kind of like tubes.

We can still change the name, once the code works.


# Running DistributedLog locally

To run this project, you need to have access to a DistributedLog write proxy. Here are the instructions, based on [this](http://distributedlog.incubator.apache.org/docs/latest/tutorials/basic-2.html) DistributedLog documentation, to get you started.

The development environment used: macOS 10.12 with HomeBrew

## Requirements

- git
- mvn

## Clone and build DistributedLog

```
$ git clone git@github.com:apache/incubator-distributedlog.git
$ cd incubator-distributedlog
$ ./scripts/change-scala-version.sh 2.11
$ mvn clean install -DskipTests
```

## ZooKeeper

HomeBrew contains ZooKeeper 3.4.9, which is suitable for DistributedLog.

```
$ brew install zookeeper
...
$ zkServer start
ZooKeeper JMX enabled by default
Using config: /usr/local/etc/zookeeper/zoo.cfg
Starting zookeeper ... STARTED
```

| port | protocol | purpose |
|---|---|---|
|2181|?|ZooKeeper|

---
Note: DistributedLog documents state that `./distributedlog-core/bin/dlog local` would start both BookKeeper and ZooKeeper, but for us, it didn't.

>It starts a zookeeper server and N bookies (N is 3 by default).

## BookKeeper

DistributedLog requires its own (Twitter's branch) kind of BookKeeper. 

This will keep running, so you might open another terminal just for it.

```
$ ./distributedlog-core/bin/dlog local 7000
./distributedlog-core/bin/dlog local 7000
JMX enabled by default
DLOG_HOME => ./distributedlog-core/bin/../../distributedlog-core
DistributedLog Sandbox is running now. You could access distributedlog://127.0.0.1:7000
```

Q: Which ports does this actually run (and with what protocols)?

| port | protocol | purpose |
|---|---|---|
|7000|?|?|

## Creating a namespace

DistributedLog streams live in namespaces. It's probably a good idea to create one, if for nothing else then for being able to start clear by just using a new name.

>If you don't want to create a separated namespace, you could use the default namespace distributedlog://127.0.0.1:7000/messaging/distributedlog.

```
$ ./distributedlog-service/bin/dlog admin bind -l /ledgers -s 127.0.0.1:7000 -c distributedlog://127.0.0.1:7000/messaging/abc
JMX enabled by default
DLOG_HOME => ./distributedlog-service/bin/..
No bookkeeper is bound to distributedlog://127.0.0.1:7000/messaging/abc
Created binding on distributedlog://127.0.0.1:7000/messaging/abc.
```

## Creating streams

Within the namespace, data is written in streams. We need to create one or multiple.

### Creating strems one by one

DistributedLog tooling seems to always want to have stream names with a "prefix" and an "expression", even for the creation of a single stream. Neither of them can be empty.

```
$ ./distributedlog-service/bin/dlog tool create -u distributedlog://127.0.0.1:7000/messaging/abc -r some- -e def
JMX enabled by default
DLOG_HOME => ./distributedlog-service/bin/..
You are going to create streams : [some-def] (Y or N) y
```

### Creating streams in bulk

```
$ ./distributedlog-service/bin/dlog tool create -u distributedlog://127.0.0.1:7000/messaging/abc -r messaging-stream- -e 1-5
JMX enabled by default
DLOG_HOME => ./distributedlog-service/bin/..
You are going to create streams : [messaging-stream-1, messaging-stream-2, messaging-stream-3, messaging-stream-4, messaging-stream-5] (Y or N) y
```

### Listing streams

```
$ ./distributedlog-service/bin/dlog tool list -u distributedlog://127.0.0.1:7000/messaging/abc
JMX enabled by default
DLOG_HOME => ./distributedlog-service/bin/..
Streams under distributedlog://127.0.0.1:7000/messaging/abc : 
--------------------------------
messaging-stream-3
messaging-stream-2
messaging-stream-1
messaging-stream-5
messaging-stream-4
--------------------------------
```

## Write Proxy

```
$ WP_SHARD_ID=1 WP_SERVICE_PORT=9000 WP_STATS_PORT=9001 WP_NAMESPACE='distributedlog://127.0.0.1:7000/messaging/abc' ./distributedlog-service/bin/dlog-daemon.sh start writeproxy
doing start writeproxy ...
starting writeproxy, logging to /Users/.../incubator-distributedlog/distributedlog-service/logs/dlog-writeproxy-...-9000.log
JMX enabled by default
DLOG_HOME => /Users/.../incubator-distributedlog/distributedlog-service/bin/..
```

The stats port (`WP_STATS_PORT`) can be used for testing that the Write Proxy is up and running:

```
$ curl localhost:9001/ping
pong
```

## Testing

This allows you to write data from the console to dynamiclog streams:

```
$ ./distributedlog-tutorials/distributedlog-basic/bin/runner run com.twitter.distributedlog.basic.ConsoleProxyMultiWriter 'inet!127.0.0.1:9000' messaging-stream-1,messaging-stream-2,messaging-stream-3,messaging-stream-4,messaging-stream-5
JMX enabled by default
DLOG_HOME => ./distributedlog-tutorials/distributedlog-basic/bin/..
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Dec 21, 2016 5:59:30 PM com.twitter.finagle.BaseResolver$$anonfun$resolvers$1 apply
INFO: Resolver[inet] = com.twitter.finagle.InetResolver(com.twitter.finagle.InetResolver@33d512c1)
Dec 21, 2016 5:59:30 PM com.twitter.finagle.BaseResolver$$anonfun$resolvers$1 apply
INFO: Resolver[fixedinet] = com.twitter.finagle.FixedInetResolver(com.twitter.finagle.FixedInetResolver@515c6049)
Dec 21, 2016 5:59:30 PM com.twitter.finagle.BaseResolver$$anonfun$resolvers$1 apply
INFO: Resolver[neg] = com.twitter.finagle.NegResolver$(com.twitter.finagle.NegResolver$@639c2c1d)
Dec 21, 2016 5:59:30 PM com.twitter.finagle.BaseResolver$$anonfun$resolvers$1 apply
INFO: Resolver[nil] = com.twitter.finagle.NilResolver$(com.twitter.finagle.NilResolver$@5fe94a96)
Dec 21, 2016 5:59:30 PM com.twitter.finagle.BaseResolver$$anonfun$resolvers$1 apply
INFO: Resolver[fail] = com.twitter.finagle.FailResolver$(com.twitter.finagle.FailResolver$@443118b0)
Dec 21, 2016 5:59:30 PM com.twitter.finagle.BaseResolver$$anonfun$resolvers$1 apply
INFO: Resolver[zk] = com.twitter.finagle.zookeeper.ZkResolver(com.twitter.finagle.zookeeper.ZkResolver@765d7657)
Dec 21, 2016 5:59:30 PM com.twitter.finagle.BaseResolver$$anonfun$resolvers$1 apply
INFO: Resolver[zk2] = com.twitter.finagle.serverset2.Zk2Resolver(com.twitter.finagle.serverset2.Zk2Resolver@74235045)
Dec 21, 2016 5:59:30 PM com.twitter.finagle.Init$$anonfun$1 apply$mcV$sp
INFO: Finagle version 6.34.0 (rev=44f444f606b10582c2da8d5770b7879ddd961211) built at 20160310-155158
[dlog] > 1st line
[dlog] > some more
[dlog] > then finish 
```

(exit with Ctrl-D)

To read the streams:

```
$ ./distributedlog-tutorials/distributedlog-basic/bin/runner run com.twitter.distributedlog.basic.MultiReader distributedlog://127.0.0.1:7000/messaging/abc messaging-stream-1,messaging-stream-2,messaging-stream-3,messaging-stream-4,messaging-stream-5
JMX enabled by default
DLOG_HOME => ./distributedlog-tutorials/distributedlog-basic/bin/..
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Opening log stream messaging-stream-1
Opening log stream messaging-stream-2
Opening log stream messaging-stream-3
Opening log stream messaging-stream-4
Opening log stream messaging-stream-5
Log stream messaging-stream-4 is empty.
Wait for records from messaging-stream-4 starting from DLSN{logSegmentSequenceNo=1, entryId=0, slotId=0}
Open reader to read records from stream messaging-stream-4
Log stream messaging-stream-2 is empty.
Wait for records from messaging-stream-2 starting from DLSN{logSegmentSequenceNo=1, entryId=0, slotId=0}
Open reader to read records from stream messaging-stream-2
Wait for records from messaging-stream-5 starting from DLSN{logSegmentSequenceNo=1, entryId=0, slotId=0}
Wait for records from messaging-stream-1 starting from DLSN{logSegmentSequenceNo=1, entryId=0, slotId=0}
Open reader to read records from stream messaging-stream-5
Open reader to read records from stream messaging-stream-1
Wait for records from messaging-stream-3 starting from DLSN{logSegmentSequenceNo=1, entryId=0, slotId=0}
Open reader to read records from stream messaging-stream-3
Received record DLSN{logSegmentSequenceNo=1, entryId=0, slotId=0} from stream messaging-stream-1
"""
Received record DLSN{logSegmentSequenceNo=1, entryId=0, slotId=0} from stream messaging-stream-5
some more
"""
"""
then finish
"""
Received record DLSN{logSegmentSequenceNo=1, entryId=0, slotId=0} from stream messaging-stream-3
"""
1st line
"""
```

(ctrl-C to break out)

That worked. :)

## Troubleshooting with zkCli

Much of DistributedLog's health etc. can be checked via ZooKeeper, aka the `zkCli` command.

```
$ zkCli
Connecting to localhost:2181
Welcome to ZooKeeper!
JLine support is enabled

WATCHER::

WatchedEvent state:SyncConnected type:None path:null
[zk: localhost:2181(CONNECTED) 0] 
```

If you see `CONNECTED`, ZooKeeper itself is running and you can use the prompt. If you see `CONNECTING`, ZooKeeper is not up.

```
[zk: localhost:2181(CONNECTED) 0] ls /
[controller_epoch, brokers, zookeeper, admin, isr_change_notification, consumers, config]
```

You can also provide commands to `zkCli` on the command line:

```
$ zkCli ls /
...
[controller_epoch, brokers, zookeeper, admin, isr_change_notification, consumers, config]
```

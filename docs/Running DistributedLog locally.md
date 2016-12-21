# Running DistributedLog locally

To run this project, you need to have access to a DistributedLog write proxy. Here are the instructions, based on [this](http://distributedlog.incubator.apache.org/docs/latest/tutorials/basic-2.html) DistributedLog documentation, to get you started.

The development environment used: macOS 10.12 with HomeBrew

## Requirements

- git

## Clone DistributedLog

```
$ git clone git@github.com:apache/incubator-distributedlog.git
$ cd incubator-distributedlog
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

## Write Proxy

Likewise, this will keep running so open another terminal.

``
$ ./distributedlog-service/bin/dlog com.twitter.distributedlog.service.DistributedLogServerApp -p 8000 --shard-id 1 -sp 8001 -u distributedlog://127.0.0.1:7000/messaging/distributedlog -mx -c distributedlog-core/conf/write_proxy.conf 
JMX enabled by default
DLOG_HOME => ./distributedlog-service/bin/..
Dec 21, 2016 3:33:13 PM com.twitter.finagle.Init$$anonfun$1 apply$mcV$sp
INFO: Finagle version 6.34.0 (rev=44f444f606b10582c2da8d5770b7879ddd961211) built at 20160310-155158
```

Note: The stats port (by `-sp` parameter) should now be usable for testing that Write Proxy is up. However:

```
$ curl localhost:8001/ping
curl: (7) Failed to connect to localhost port 8001: Connection refused
```

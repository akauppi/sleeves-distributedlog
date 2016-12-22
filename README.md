# sleeves-distributedlog

Accessing [DistributedLog](http://distributedlog.incubator.apache.org/) persistent message bus from Scala as Akka Streams.

## Requirements

- sbt

Development environment is macOS 10.12 with HomeBrew.

## Purpose

DistributedLog is a promising persistent message bus, similar to Kafka but with different design approach and constraints / freedoms. It for example:

- has no concept of partitions (which are needed in Kafka to scale writes)
- allows adding more cluster space simply by adding more BookKeeper nodes

The DistributedLog access happens via Thrift, not REST API. The purpose of this library is to do Akka Strems -> Thrift -> Akka Streams conversions, so that the application does not need to be aware of DistributedLog internals.

## Approach

- Try to avoid dependencies to DistributedLog libraries, if we can (REST API mental approach though likely with Thrift)

## Status

The project is in early stages. I hope to have something meaningful within a month (21-Dec-16).

## Running tests

Disclaimer: *eventually*, we'll make `sbt test` run locally, without the need for setting up anything (it may use `scala-it-docker` to pull in DistributedLog via Docker).

For now, you need to set up:

- distributedlog Write Proxy to `localhost:9000`
- create a namespace `sleeves-test`
- within the namespace, create stream named `test`

These parameters are found in the `src/test/resources/application.conf` file.

### Run Write Proxy

Instructions in docs/[Running DistributedLog Locally](docs/Running DistributedLog locally.md)

### Create the namespace

```
$ ./distributedlog-service/bin/dlog admin bind -l /ledgers -s 127.0.0.1:7000 -c distributedlog://127.0.0.1:7000/messaging/sleeves-test
```

### Create the stream

```
$ ./distributedlog-service/bin/dlog tool create -u distributedlog://127.0.0.1:7000/messaging/sleeves-test -r te -e st
```

### Checking all is ready

```
$ ./distributedlog-service/bin/dlog tool list -u distributedlog://127.0.0.1:7000/messaging/sleeves-test
...
Streams under distributedlog://127.0.0.1:7000/messaging/sleeves-test : 
--------------------------------
test
--------------------------------
```

### Finally...

```
$ sbt test
```

### Seeing what the tests wrote

```
$ ./distributedlog-tutorials/distributedlog-basic/bin/runner run com.twitter.distributedlog.basic.MultiReader distributedlog://127.0.0.1:7000/messaging/sleeves-test test
```
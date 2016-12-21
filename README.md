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

```
$ sbt test
```

These tests check the internals, and don't require a DistributedLog instance to be running.

<!-- enable once we have integration tests

Start up DistributedLog locally, see docs/[Running DistributedLog locally.md](docs/Running DistributedLog locally.md)
-->


<!-- remove
## Installing DL 0.4.0-SNAPSHOT locally

DistributedLog 0.4.0 has not been released, yet (20-Dec-16). You need to build
and install it locally by:

```
$ git clone git@github.com:apache/incubator-distributedlog.git
$ cd incubator-distributedlog
$ ./scripts/change-scala-version.sh 2.11
```
-->
package test

import org.scalatest.{FlatSpec, Matchers}

/*
* Test that Zookeeper is reachable.
*/
class ZookeeperTest extends FlatSpec with Matchers with DockerZookeeperService {
  import ZookeeperTest._

  behavior of "Zookeeper"

  // tbd. Not sure how to do that (command line has many options, but it seems ZooKeeper doesn't have an HTTP interface?
  //
  // Leave this out - we can do it easily manually.
  //
  // http://stackoverflow.com/questions/29106546/how-to-check-if-zookeeper-is-running-or-up-from-command-prompt

  it should "pong to a ping" ignore /*it*/ {
  }
}

object ZookeeperTest {
  val port: Int = 2181
}
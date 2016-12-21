package test

import akka.http.scaladsl.model.StatusCodes
import org.scalatest.{FlatSpec, Matchers}

/*
* Test that BookKeeper is reachable.
*/
class BookKeeperTest extends FlatSpec with Matchers with DockerBookKeeperService {
  import BookKeeperTest._

  behavior of s"BookKeeper status port ($port)"

  it should "pong to a ping" ignore /*it*/ {

    // tbd. Get from cb code a sample

    /***
    whenReady ...Get("/ping") ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "pong"
    }

    $ curl localhost:9001/ping
    ***/
  }
}

object BookKeeperTest {
  val port: Int = 9001
}
package test

import java.net.URL

import org.scalatest.{FlatSpec, Matchers}
import sleeves.distributedlog.DLClient

class DLClientTest extends FlatSpec with Matchers {
  import DLClientTest._

  behavior of "DLClient"

  it should "be createable" in {
    DLClient("sleeves-test", host, port)
  }
}

object DLClientTest {
  val host = "127.0.0.1"
  val port = 9000
}

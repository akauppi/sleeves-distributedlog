package test

import org.scalatest.{FlatSpec, Matchers}
import sleeves.distributedlog.{DLClient}

import scala.util.Random

// Requirements for running the tests:
//
// - namespace 'sleeves-test' (name in conf) has been manually created
//
class DLClientTest extends FlatSpec with Matchers {
  import DLClientTest._
  import Conf.{host,port,namespace}

  behavior of "DLClient"

  it should s"be createable to a prepared namespace ($namespace)" in {
    val client = DLClient(clientId, namespace, host, port)
    client.close()
  }

  it should s"not be createable to a non-existing namespace" in {
    val ns = f"nosuch-${random.nextInt(1000)}%04d"   // 0000..9999

    an [Int] should be thrownBy {
      val client = DLClient(clientId, ns, host, port)
      client.close()
    }
  }
}

object DLClientTest {
  val clientId = "DLClientTest"

  val random = Random
}

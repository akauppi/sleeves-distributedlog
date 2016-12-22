package test

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.twitter.distributedlog.DLSN
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import sleeves.distributedlog.{DLClient, DLReader, DLWriter}

import scala.collection.immutable
import scala.concurrent.Future
import scala.util.Random

class DLReaderTest extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll {
  import DLReaderTest._
  import Conf.{host,port,namespace,streamname}

  implicit val as = ActorSystem("DLReaderTest")
  implicit val mat = ActorMaterializer()

  behavior of "DLReader"

  // tbd. Once/if we have auto-closing client, we can simply initialize it as 'val' here

  var client: DLClient = null   // initialized in 'beforeAll'

  override
  def beforeAll(): Unit = {
    client = DLClient(clientId, namespace, host, port)
  }

  override
  def afterAll(): Unit = {
    client.close()
    client = null
  }

  it should "be possible to read from a stream (and get DLSNs for the log entries)" in {

    val src: Source[Tuple2[Array[Byte],DLSN],_] = new DLReader(client, streamname).source

    val outFut: Future[Seq[Tuple2[Array[Byte],DLSN]]] = src
      .runWith(Sink.seq)

    whenReady(outFut) { seq =>
      info(seq.toString)    //seq.foreach{ dlsn => info(dlsn.toString) }

      seq.size should (be > 0)
    }
  }

  it should "not be possible to write to a stream that does not exist" ignore /*in*/ {
    val stream: String = f"nosuch-${random.nextInt(1000)}%04d"

    a [Nothing] should be thrownBy {    // tbd.
    val writer = new DLWriter(client, stream)
    }
  }
}

object DLReaderTest {
  val clientId = "DLReaderTest"
  val random = Random
}

package test

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.twitter.distributedlog.DLSN
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import sleeves.distributedlog.{DLClient, DLWriter}

import scala.collection.immutable
import scala.concurrent.Future

class DLWriterTest extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll {
  import DLWriterTest._
  import Conf.{host,port,namespace,streamname}

  implicit val as = ActorSystem("DLWriterTest")
  implicit val mat = ActorMaterializer()

  behavior of "DLWriter"

  // tbd. Once/if we have auto-closing client, we can simply initialize it as 'val' here

  var client: DLClient = null   // initialized in 'beforeAll'

  override
  def beforeAll(): Unit = {
    client = DLClient(clientId, namespace, host, port)
  }

  override
  def afterAll(): Unit = {
    client.close()
  }

  it should "be possible to write to a stream (and get DLSN's back)" in {
    val client = DLClient("sleeves-test2", namespace, host, port)

    val flow: Flow[Array[Byte],DLSN,_] = new DLWriter(client, streamname).flow

    val data = Seq("It's hard work", "to do less")

    val outFut: Future[Seq[DLSN]] = Source(data.to[immutable.Seq]).map( _.getBytes )
      .via(flow)
      .runWith(Sink.seq)

    whenReady(outFut) { seq =>
      info(seq.toString)    //seq.foreach{ dlsn => info(dlsn.toString) }

      seq.size should ((be > 0) and (be <= data.length))
    }
  }

  // tbd. Test: writing to a non-existing streamName should fail
}

object DLWriterTest {
  val clientId = "DLWriterTest"
}

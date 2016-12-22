package sleeves.distributedlog

import java.nio.ByteBuffer

import com.twitter.distributedlog.DLSN
import com.twitter.distributedlog.service.{DistributedLogClient, DistributedLogClientBuilder}
import com.twitter.finagle.thrift.ClientId

import scala.util.{Failure, Success, Try}

/*
* References:
*   Basic Tutorial - Write Records using Write Proxy Client
*     -> http://distributedlog.incubator.apache.org/docs/latest/tutorials/basic-2.html
*/

/*
* Q: What's the difference between 'clientId' and 'name'; DistributedLog sample uses same string for both?
*
* clientId:   (any string)
* name:       (use same as 'clientId')
* finagleNameStr: e.g. "inet!127.0.0.1:9000" (the IP of the write proxy)
*/
class DLClient private (clientId: String, name: String, finagleNameStr: String) {
  import com.twitter.util.{FutureEventListener, Future => TFuture}

  private
  val client: DistributedLogClient = {
    DistributedLogClientBuilder.newBuilder()
      .clientId( ClientId(clientId) )
      .name(name)
      .thriftmux(true)
      .finagleNameStr(finagleNameStr)   // Q: What's the purpose of the Finagle name?
      .build()
  }

  // Used by 'DLWriter'
  //
  // Distributionlog API specific details are handled here (such as use of 'com.twitter.util.Future' instead of normal
  // Scala classes); Akka stream belong to 'DLWriter'.
  //
  private[distributedlog]
  def write( streamName: String, data: Array[Byte], cb: Function1[Try[DLSN],Unit] ): Unit = {    // tbd. can change the return value to a (Scala) Future if needed

    val fel = new FutureEventListener[DLSN] {
      override
      def onSuccess(value: DLSN) {
        cb( Success(value) )
      }
      override
      def onFailure(ex: Throwable) {
        cb(Failure(ex))
      }
    }

    val wrTFut: TFuture[DLSN] = client.write(streamName, ByteBuffer.wrap(data))
    wrTFut.addEventListener(fel)
  }

  // Note: Scala does not have class destructors. If the caller does not explicitly close the handle, it will
  //    leak memory (since 'client' won't get closed).
  //
  def close(): Unit = {
    client.close()
  }
}

object DLClient {

  def apply(clientId: String, nameSpace: String, host: String, port: Int): DLClient = {

    // tbd. What to do with 'nameSpace'?

    new DLClient(clientId, "" /*name (is optional, we could even remove it?)*/, s"inet!$host:$port")
  }
}


/* REMOVE
// Helper actor for taking in 'DLSN' values and passing the latest one of them out to a stream.
//
// References:
//    Pre Materialization with Actor
//      -> http://stackoverflow.com/questions/30964824/how-to-create-a-source-that-can-receive-elements-later-via-a-method-call
//
private
object DLSNPublisher {
  def apply()(implicit as: ActorSystem): Publisher[DLSN] = {
    val actorRef: ActorRef = as actorOf Props[Forwarder]
    ActorPublisher[DLSN](actorRef)
  }

  private
  class Forwarder extends Actor {
    private
    var largest: DLSN = null

    override
    def receive = {
      case x: DLSN if ((largest == null) || (x.compareTo(largest) > 0)) =>    // later DLSN than before

    }
  }
}
*/

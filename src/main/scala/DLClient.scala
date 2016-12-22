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
* clientId:   (any string?) tbd. document better
* nameSpace:  (e.g. "..." tbd. document better)
* finagleNameStr: e.g. "inet!127.0.0.1:9000" (the IP of the write proxy)
*/
class DLClient private (clientId: String, nameSpace: String, finagleNameStr: String) {
  import com.twitter.util.{FutureEventListener, Future => TFuture}

  private
  val client: DistributedLogClient = {
    DistributedLogClientBuilder.newBuilder()
      .clientId( ClientId(clientId) )
      .name(nameSpace)                  // Q: is this the namespace? TBD: NOT SURE! what is it - do we need it? Where to place the namespace?
      .thriftmux(true)
      .finagleNameStr(finagleNameStr)   // carries host and port (we always use 'inet', i.e. reach for Write Proxy)
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

  // Q: How to do automatic closing of resources properly in Scala? Now, the responsibility lies on the caller.
  //
  def close(): Unit = {
    client.close()
  }
}

object DLClient {

  def apply(clientId: String, nameSpace: String, host: String, port: Int): DLClient = {

    new DLClient(clientId, nameSpace, s"inet!$host:$port")
  }
}

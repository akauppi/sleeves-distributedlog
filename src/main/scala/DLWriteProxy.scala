package sleeves.distributedlog

import java.nio.ByteBuffer

import com.twitter.distributedlog.DLSN
import com.twitter.distributedlog.service.{DistributedLogClient, DistributedLogClientBuilder}
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.util.FutureEventListener

/*
*/
class DLWriteProxy {


}

object DLWriteProxy {

}



object SampleCode {   // from -> http://distributedlog.incubator.apache.org/docs/latest/user_guide/api/proxy.html
  import com.twitter.util.Duration
  import com.twitter.finagle.thrift.ClientId

  {
    // 1. Create a Finagle client builder

    val clientBuilder /*: ClientBuilder[_,_,_,_,_]*/ = ClientBuilder.get()
      .hostConnectionLimit(1)
      .hostConnectionCoresize(1)
      .tcpConnectTimeout(Duration.fromMilliseconds(200))
      .connectTimeout(Duration.fromMilliseconds(200))
      .requestTimeout(Duration.fromSeconds(2))

    // 2. Choose a client id to identify the client.

    val clientId: ClientId = ClientId("test")

    val finagleName: String = "inet!127.0.0.1:8000"

    // 3. Create the write proxy client builder

    val builder: DistributedLogClientBuilder = DistributedLogClientBuilder.newBuilder()
      .name("test-writer")
      .clientId(clientId)
      .clientBuilder(clientBuilder)
      //.statsReceiver(statsReceiver)
      .finagleNameStr(finagleName)

    // 4. Build the client

    val client: DistributedLogClient = builder.build()
  }

  {
    val client: DistributedLogClient = ???

    // Write a record to a stream
    val streamName: String = "test-stream"
    val data: Array[Byte] = ???
    val writeFut: com.twitter.util.Future[DLSN] = client.write(streamName, ByteBuffer.wrap(data))
    com.twitter.util.Await.result(writeFut)
  }

  {
    val client: DistributedLogClient = ???

    // Truncate a stream to DLSN
    val streamName: String = "test-stream"
    val truncationDLSN: DLSN = ???
    val truncateFuture: com.twitter.util.Future[_] = client.truncate(streamName, truncationDLSN)
    com.twitter.util.Await.result(truncateFuture)
  }
}

object SampleCode2 {    // from -> http://distributedlog.incubator.apache.org/docs/latest/tutorials/basic-2.html
  import com.twitter.finagle.thrift.ClientId

  {
    val finagleName: String = "inet!127.0.0.1:8000"

    val client: DistributedLogClient = DistributedLogClientBuilder.newBuilder()
      .clientId(ClientId.apply("console-proxy-writer"))
      .name("console-proxy-writer")
      .thriftmux(true)
      .finagleNameStr(finagleName)
      .build()
  }

  {
    val client: DistributedLogClient = ???
    val streamName: String = "basic-stream-2"
    val data: Array[Byte] = ???
    val writeFuture: com.twitter.util.Future[DLSN] = client.write(streamName, ByteBuffer.wrap(data))

    writeFuture.addEventListener(new FutureEventListener[DLSN]() {
      override
      def onSuccess(value: DLSN): Unit = {
        // executed when write completed
      }

      override
      def onFailure(cause: Throwable): Unit = {
        // executed when write failed
      }
    })

    client.close()
  }
}


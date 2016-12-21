package sleeves.distributedlog

import java.net.URL
import java.nio.ByteBuffer

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.OverflowStrategy
import akka.stream.actor.ActorPublisher
import akka.stream.javadsl.SourceQueue
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import com.twitter.distributedlog.DLSN
import com.twitter.distributedlog.service.{DistributedLogClient, DistributedLogClientBuilder}
import com.twitter.finagle.thrift.ClientId
import com.twitter.util.{FutureEventListener, Future => TFuture}
import org.reactivestreams.Publisher

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

  private
  val client: DistributedLogClient = {
    DistributedLogClientBuilder.newBuilder()
      .clientId( ClientId(clientId) )
      .name(name)
      .thriftmux(true)
      .finagleNameStr(finagleNameStr)   // Q: What's the purpose of the Finagle name?
      .build()
  }

  // Writing happens via a Flow - data in, DLSN information on the writes out. This should actually be
  // rather natural concept for the caller, hopefully. :)
  //
  def flow(streamName: String): Flow[Array[Byte],DLSN,_] = {

    var sq: SourceQueueWithComplete[DLSN] = null

    // The source that will get triggered once confirmation of write arrives
    //
    // Note: The strategy could be either 'dropHead' (forget oldest) or 'backpressure' (which does not drop anything).
    //    We need to see if retaining order is important, or just getting a confirmation on the highest offset written
    //    out (i.e. can we drop entries). The 'max' makes sure the largest always gets through, so that may already
    //    drop values (if order changes in future completion). To be studied closer. AKa211216
    //
    // Note: It's important we return the source from '.mapMaterializedValue'. The inner block gets called
    //    once the whole flow is materialized, but that is enough for use by the sink. Alternative to this would
    //    be to use an actor setup of some kind. If this works, great.
    //
    // References:
    //    Provide a 'Source.queue' ... (Akka GitHub Issues)
    //      -> https://github.com/akka/akka/issues/17693
    //    How can I use ... Source queue to the caller without materializing it?
    //      -> http://stackoverflow.com/questions/37113877/how-can-i-use-and-return-source-queue-to-caller-without-materializing-it/37117205#37117205
    //
    val src: Source[DLSN,_] = Source.queue(10,OverflowStrategy.dropHead).mapMaterializedValue( (x: SourceQueueWithComplete[DLSN]) => sq = x )

    // The sink that takes in the data
    //
    val sink: Sink[Array[Byte],_] = Sink.foreach( (data: Array[Byte]) => {

      val wrFuture: TFuture[DLSN] = client.write(streamName, ByteBuffer.wrap(data))

      val fel = new FutureEventListener[DLSN] {
        override
        def onSuccess(value: DLSN) {
          sq.offer(value)
        }
        override
        def onFailure(ex: Throwable) {
          sq.fail(ex)
        }
      }

      wrFuture.addEventListener(fel)
    })

    Flow.fromSinkAndSource(sink,src)
  }

  // Note: Scala does not have class destructors. If the caller does not explicitly close the handle, it will
  //    leak memory (since 'client' won't get closed).
  //
  def close(): Unit = {
    client.close()
  }
}

object DLClient {

  def apply(clientId: String, host: String, port: Int): DLClient = {
    new DLClient(clientId, clientId, s"inet!$host:$port")
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

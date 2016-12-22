package sleeves.distributedlog

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import com.twitter.distributedlog.DLSN

import scala.util.{Failure, Success, Try}

/*
* Writing to a certain distributedlog stream
*/
class DLWriter( client: DLClient, streamName: String ) {

  // Writing happens via a Flow - data in, DLSN information on the writes out. This should actually be
  // rather natural concept for the caller, hopefully. :)
  //
  def flow: Flow[Array[Byte],DLSN,_] = {

    var sq: SourceQueueWithComplete[DLSN] = null

    var latest: DLSN = null

    def callback(t: Try[DLSN]): Unit = t match {
      case Success(v) if (latest == null) || (v.compareTo(latest) > 0) => sq.offer(v)
      case Success(_) =>  // nothing (had already passed a larger offset
      case Failure(ex) => sq.fail(ex)
    }

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
    val src: Source[DLSN,_] = Source.queue(10,OverflowStrategy.dropHead)
      .mapMaterializedValue( (x: SourceQueueWithComplete[DLSN]) => sq = x )

    // The sink that takes in the data
    //
    val sink: Sink[Array[Byte],_] = Sink.foreach( (data: Array[Byte]) => {
      client.write(streamName, data, callback)
    })

    Flow.fromSinkAndSource(sink,src)
  }
}

object DLWriter {
}

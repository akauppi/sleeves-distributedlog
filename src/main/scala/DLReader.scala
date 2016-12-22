package sleeves.distributedlog

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import com.twitter.distributedlog.DLSN

import scala.util.{Failure, Success, Try}

/*
* Read from a certain distributedlog stream
*
* References:
*   Tail reading records from a stream
*     -> http://distributedlog.incubator.apache.org/docs/latest/tutorials/basic-5.html
*/
class DLReader( client: DLClient, streamName: String ) {

  def source: Source[Tuple2[Array[Byte],DLSN],_] = {

    ???
  }
}

object DLReader {
}

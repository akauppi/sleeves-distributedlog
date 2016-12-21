package sleeves.distributedlog

import java.io.IOException
import java.net.URI

import com.google.common.base.{Optional => GOptional}
import com.twitter.distributedlog._
import com.twitter.distributedlog.namespace.{DistributedLogNamespace, DistributedLogNamespaceBuilder}
import com.twitter.util.FutureEventListener

import scala.collection.mutable.ArrayBuffer

/*
* Trying the Java API
*
* Ref.
*   Core Library API -> http://distributedlog.incubator.apache.org/docs/latest/user_guide/api/core
*/
class Main extends App {

  //--- Namespace API ---

  // DistributedLog Configuration
  val conf = new DistributedLogConfiguration()

  // Namespace URI
  val uri = new URI("...")

  // create a builder to build namespace instances
  val namespace: DistributedLogNamespace = DistributedLogNamespaceBuilder.newBuilder()
    .conf(conf)             // configuration that used by namespace
    .uri(uri)               // namespace uri
    .statsLogger(???)       // stats logger to log stats
    .featureProvider(???)   // feature provider on controlling features
    .build()

  try {
    namespace.createLog("test-log")
  } catch {
    case ex: IOException => // handling the exception on creating a log
  }

  val conf2 = new DistributedLogConfiguration()
  conf2.setCreateStreamIfNotExists(true)
  val namespace2: DistributedLogNamespace = DistributedLogNamespaceBuilder.newBuilder()   // note: doc didn't have the 'Builder'
    .conf(conf2)
    //...
    .build()

  val logManager = namespace2.openLog("test-log")
  // use the log manager to open writer to write data or open reader to read data
  //...

  {
    val conf = new DistributedLogConfiguration()
    // set the retention period hours to 24 hours.
    conf.setRetentionPeriodHours(24)
    val uri = new URI(???)
    val namespace = DistributedLogNamespaceBuilder.newBuilder()
      .conf(conf)
      .uri(uri)
      //...
      .build()

    // Per Log Configuration
    val logConf = new DistributedLogConfiguration()
    // set the retention period hours to 12 hours for a single stream
    logConf.setRetentionPeriodHours(12)

    // open the log with overrided settings
    val logManager: DistributedLogManager = namespace.openLog("test-log",
      GOptional.of(logConf),
      GOptional.absent()
    )
  }

  {
    val namespace: DistributedLogNamespace = ???
    try {
      namespace.deleteLog("test-log")
    } catch {
      case ex: IOException => // handle the exceptions
    }
  }

  {
    val namespace: DistributedLogNamespace = ???
    if (namespace.logExists("test-log")) {
      // actions when log exists
    } else {
      // actions when log doesn't exist
    }
  }

  {
    import collection.JavaConverters._

    val namespace: DistributedLogNamespace = ???
    val logs: Iterator[String] = namespace.getLogs
    while (logs.hasNext) {
      val logName: String = logs.next()
      // ... process the log
    }
  }

  //--- Writer API ---
  {
    val namespace: DistributedLogNamespace = ???
    val dlm = namespace.openLog("test-log")
    val writer: LogWriter = dlm.startLogSegmentNonPartitioned()
  }

  {
    val txid = 1L
    val data: Array[Byte] = ???
    val record = new LogRecord(txid, data)
  }

  {
    import collection.JavaConverters._

    val writer: LogWriter = ???
    val record: LogRecord = ???

    writer.write(record)
    // or
    val records: Seq[LogRecord] = Seq(record)
    writer.writeBulk(records.toList.toJava)
  }

  {
    val writer: LogWriter = ???
    // flush the records
    writer.setReadyToFlush()
    // commit the records to make them visible to readers
    writer.flushAndSync()
  }

  {
    val writer: LogWriter = ???
    writer.markEndOfStream()
  }

  /***
DistributedLogNamespace namespace = ....;
DistributedLogManager dlm = namespace.openLog("test-log");

LogWriter writer = dlm.startLogSegmentNonPartitioned();
for (long txid = 1L; txid <= 100L; txid++) {
    byte[] data = ...;
    LogRecord record = new LogRecord(txid, data);
    writer.write(record);
}
// flush the records
writer.setReadyToFlush();
// commit the records to make them visible to readers
writer.flushAndSync();

// seal the log stream
writer.markEndOfStream();
    */

  {
    val namespace: DistributedLogNamespace = ???
    val dlm: DistributedLogManager = namespace.openLog("test-log")
    val writer: AsyncLogWriter = dlm.startAsyncLogSegmentNonPartitioned()

    val futs: Seq[com.twitter.util.Future[DLSN]] =
      for (txid <- 1 to 100) yield {
        val data: Array[Byte] = ???
        val record = new LogRecord(txid, data)
        writer.write(record)
      }

    val results: Seq[DLSN] = com.twitter.util.Await.result( com.twitter.util.Future.collect(futs) )
  }

  {
    val writer: AsyncLogWriter = ???
    val truncateDLSN: DLSN = ???
    val truncateFuture: com.twitter.util.Future[java.lang.Boolean /*DLSN*/] = writer.truncate(truncateDLSN)
    // wait for truncation result
    com.twitter.util.Await.result(truncateFuture)
  }

  //--- Reader API ---

  {
    val dlm: DistributedLogManager = ???
    var nextTxId: Long = ???
    var reader: LogReader = dlm.getInputStream(nextTxId)

    while (true) { // keep reading & processing records
      try {
        val record: LogRecord = reader.readNext(false)
        nextTxId = record.getTransactionId
        // process the record
        //...
      } catch {
        case ex: IOException =>  // handle the exception
          //...
          reader = dlm.getInputStream(nextTxId + 1)
      }
    }
  }

  //... skipped some

  {
    def readBulk(reader: AsyncLogReader, n: Int): Unit = {
      reader.readBulk(n).addEventListener(new FutureEventListener[java.util.List[LogRecordWithDLSN]]() {
        def onSuccess(records: java.util.List[LogRecordWithDLSN]): Unit = {
          // process the records
          //...
          // read next
          readBulk(reader, n)
        }
        def onFailure(cause: Throwable): Unit = {
          // handle errors and re-create reader
          //...
          val nextReader = ???
          // read next
          readBulk(nextReader, n)   // hmm.. adds up call stack?
        }
      })
    }

    val reader: AsyncLogReader = ???
    readBulk(reader, 100)
  }
}

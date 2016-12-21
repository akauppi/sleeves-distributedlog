package sleeves.distributedlog

import java.net.URL

import com.twitter.distributedlog.service.{DistributedLogClient, DistributedLogClientBuilder}
import com.twitter.finagle.thrift.ClientId

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
}

object DLClient {

  def apply(clientId: String, host: String, port: Int): DLClient = {
    new DLClient(clientId, clientId, s"inet!$host:$port")
  }
}

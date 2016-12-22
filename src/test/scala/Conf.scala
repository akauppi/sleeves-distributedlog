package test

import com.typesafe.config.ConfigFactory

object Conf {
  private
  val c = ConfigFactory.load.getConfig("test")

  val host: String = c.getString("distributedlog.host")
  val port: Int = c.getInt("distributedlog.port")
  val namespace: String = c.getString("distributedlog.namespace")
  val streamname: String = c.getString("distributedlog.streamname")
}

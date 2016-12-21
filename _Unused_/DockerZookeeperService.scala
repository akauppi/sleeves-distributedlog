package test

import com.whisk.docker.{DockerContainer, DockerKit, DockerReadyChecker}

trait DockerZookeeperService extends DockerKit {
  import DockerZookeeperService._

  val zkContainer = DockerContainer("31z4/zookeeper:latest")
    //.withPorts(DefaultMongodbPort -> None)
    .withReadyChecker(DockerReadyChecker.LogLineContains("waiting for connections on port"))
    .withCommand("mongod", "--nojournal", "--smallfiles", "--syncdelay", "0")

  abstract override def dockerContainers: List[DockerContainer] = /*mongodbContainer ::*/ super.dockerContainers
}

object DockerZookeeperService {

  val port = 2181
}

package com.vovapolu.esenin
package server

import java.nio.file.Paths

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.messages._

import scala.collection.JavaConverters._
import scala.util.Try

object EseninServerMain extends App {

  def toDockerContainer(module: config.ModuleConfig): ContainerConfig = {
    module match {
      case config.ModuleConfig(name, _, config.DockerHubSource(image)) =>
        ContainerConfig.builder
          .image(s"$image:latest")
          .hostname(name)
          .build()
    }
  }

  val proxyContainer = {
    val port = config.Constants.proxyPort.toString

    val hostConfig =
      HostConfig.builder
        .portBindings(
          Map(port -> List(PortBinding.of("0.0.0.0", port)).asJava).asJava)
        .build

    ContainerConfig.builder
      .hostConfig(hostConfig)
      .image("esenin/proxy:latest")
      .exposedPorts(port)
      .build
  }

  val res = for {
    modulesConfig <- config.loadModules(
      Paths.get(s"./${config.Constants.modulesConfFilename}")
    )
    docker <- Try { DefaultDockerClient.fromEnv.build }.toEither
    network <- Try {
      docker.createNetwork(NetworkConfig.builder().name("esenin-net").build())
    }.toEither
    containers = modulesConfig.modules.map(toDockerContainer) :+ proxyContainer
    creations <- Try { containers.map(docker.createContainer) }
    _ <- Try { creations.foreach(c => docker.connectToNetwork(c.id, network.id)) }.toEither
    _ <- Try { creations.foreach(c => docker.startContainer(c.id)) }.toEither
    _ <- Try { scala.io.StdIn.readLine() }.toEither
    _ <- Try { creations.foreach(c => docker.killContainer(c.id)) }.toEither
    _ <- Try { creations.foreach(c => docker.removeContainer(c.id)) }.toEither
    res <- Try { docker.close() }.toEither
  } yield res

  res match {
    case Left(err) => println(err)
    case Right(_) => println("Docker is closed.")
  }
}

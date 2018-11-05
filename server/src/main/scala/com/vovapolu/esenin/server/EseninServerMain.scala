package com.vovapolu.esenin
package server

import java.nio.file.Paths

import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import com.spotify.docker.client.messages.HostConfig.Bind
import com.spotify.docker.client.messages._

import scala.collection.JavaConverters._
import scala.concurrent.{Await, Future}
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object EseninServerMain extends App {

  val configPath = Paths.get(s"./${config.Constants.modulesConfFilename}")

  def toDockerContainer(module: config.ModuleConfig): ContainerConfig = {
    module match {
      case config.ModuleConfig(name, _, config.DockerHubSource(image)) =>
        ContainerConfig.builder
          .image(s"$image:latest")
          .build()
    }
  }

  def monitorContainers(docker: DockerClient,
                        ids: Seq[String]): Either[String, Unit] = {
    ids.map { id =>
      val info = docker.inspectContainer(id)
      if (!info.state().running()) {
        if (info.state().exitCode() == 0) {
          docker.restartContainer(id)
          None
        } else {
          Some(s"Error in $id container: ${info.state().error()}")
        }
      } else {
        None
      }
    }.collectFirst {
      case Some(err) => err
    }.toLeft(())
  }

  val proxyContainer = {
    val port = config.Constants.proxyPort.toString

    val hostConfig =
      HostConfig.builder
        .portBindings(
          Map(port -> List(PortBinding.of("0.0.0.0", port)).asJava).asJava)
        .appendBinds(
          Bind
            .from(configPath.toAbsolutePath.toString)
            .to(s"/etc/${config.Constants.modulesConfFilename}")
            .readOnly(true)
            .build)
        .build

    ContainerConfig.builder
      .hostConfig(hostConfig)
      .image("esenin/proxy:latest")
      .exposedPorts(port)
      .build
  }

  val res = for {
    modulesConfig <- config.loadModules(configPath)
    docker <- Try {
      println("Starting the docker...")
      DefaultDockerClient.fromEnv.build
    }.toEither
    network <- Try {
      println("Creating esenin-net network...")
      docker
        .listNetworks()
        .asScala
        .filter(_.name == "esenin-net")
        .foreach(n => docker.removeNetwork(n.id))
      docker.createNetwork(NetworkConfig.builder().name("esenin-net").build())
    }.toEither
    containers = ("proxy" -> proxyContainer) +:
      modulesConfig.modules.map(m => m.name -> toDockerContainer(m))
    creations <- Try {
      println(
        s"Creating containers: ${containers.map(_._2.image).mkString(", ")}")
      containers.map { case (name, c) => docker.createContainer(c, name) }
    }.toEither
    _ <- Try {
      println("Connecting containers to the network...")
      creations.foreach(c => docker.connectToNetwork(c.id, network.id))
    }.toEither
    _ <- Try {
      println("Starting containers...")
      creations.foreach(c => docker.startContainer(c.id))
    }.toEither
    _ <- {
      val monitor = Future {
        var res: Either[String, Unit] = Right(())
        while (res.isRight) { // TODO looks hacky
          Thread.sleep(1000) // TODO looks hacky
          res = monitorContainers(docker, creations.map(_.id))
        }
        res
      }
      val pressEnter = Future {
        println("Press any key to stop esenin server.")
        scala.io.StdIn.readLine()
      }.map(_ => Right(()))
      val res = Await.result(Future.firstCompletedOf(Seq(monitor, pressEnter)), Duration.Inf)
      res.left.map(new RuntimeException(_)).toTry
    }.toEither
    _ <- Try {
      println("Killing containers...")
      creations.foreach(c => docker.killContainer(c.id))
    }.toEither
    _ <- Try {
      println("Removing containers...")
      creations.foreach(c => docker.removeContainer(c.id))
    }.toEither
    res <- Try {
      println("Closing docker...")
      docker.close()
    }.toEither
  } yield res

  res match {
    case Left(err) => println(s"Error was occurred: $err")
    case Right(_)  => println("Docker is closed.")
  }
}

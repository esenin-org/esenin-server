package com.vovapolu.nlpserver

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.messages.{
  ContainerConfig,
  HostConfig,
  PortBinding
}

import collection.JavaConverters._

object NlpServerMain extends App {
  val docker = DefaultDockerClient.fromEnv.build

  val hostConfig = HostConfig.builder
    .portBindings(
      Map("9000" -> List(PortBinding.of("0.0.0.0", "9000")).asJava).asJava)
    .build

  val containerConfig = ContainerConfig.builder
    .hostConfig(hostConfig)
    .image("nlp-server/syntaxnet:latest")
    .exposedPorts("9000")
    .build

  val creation = docker.createContainer(containerConfig)
  val id = creation.id

  docker.startContainer(id)
  docker.killContainer(id)
  docker.removeContainer(id)

  docker.close()
}

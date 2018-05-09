package com.vovapolu.esenin

import java.nio.file.Path

import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.error.ConfigReaderFailures

import scala.util.Try

package object config {
  object Constants {
    val proxyPort = 9000
    val modulesConfFilename = "modules.conf"
    val containersConfigFilename = "containers.conf"
  }

  import ConfigConverters._

  def loadModules(path: Path): Either[List[String], ModulesConfig] = {
    pureconfig.loadConfig[ModulesConfig](ConfigFactory.parseFile(path.toFile)).left.map(_.toList.map(_.description))
  }
  def saveContainers(config: ContainersConfig, path: Path): Either[Throwable, Unit] = {
    Try { pureconfig.saveConfigAsPropertyFile(ConfigWriter[ContainersConfig].to(config), path) }.toEither
  }
  def loadContainers(path: Path): Either[ConfigReaderFailures, ContainersConfig] = {
    pureconfig.loadConfig[ContainersConfig](ConfigFactory.parseFile(path.toFile))
  }
}

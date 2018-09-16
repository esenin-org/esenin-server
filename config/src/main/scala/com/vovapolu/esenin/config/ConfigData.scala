package com.vovapolu.esenin
package config

import pureconfig._
import pureconfig.error.CannotConvert

sealed trait NlpFunc {
  def name: String
}
case object POS extends NlpFunc {
  val name = "POS"
}
case object TM extends NlpFunc {
  val name = "TM"
}

sealed trait ModuleSource
final case class DockerHubSource(image: String) extends ModuleSource {
  require(!image.contains(":"), "image shouldn't contain colon")
}

final case class ModuleConfig(name: String,
                              nlpFunc: NlpFunc,
                              source: ModuleSource)
final case class ModulesConfig(modules: Seq[ModuleConfig])
final case class ContainerConfig(module: ModuleConfig)
final case class ContainersConfig(containers: Seq[ContainerConfig])

object ConfigConverters {
  implicit val nlpFuncHint = new EnumCoproductHint[NlpFunc]

  implicit val moduleSourceReader
    : ConfigReader[ModuleSource] = ConfigReader[String].emap(
    s => {
      val parts = s.split(':')
      if (parts.length != 2) {
        Left(CannotConvert(s, "ModuleSource", "Wrong number of colons"))
      } else {
        parts(0) match {
          case "dockerhub" => Right(DockerHubSource(parts(1)))
          case _ =>
            Left(
              CannotConvert(s, "ModuleSource", s"Unknown source: ${parts(0)}"))
        }
      }
    }
  )

  implicit val moduleSourceWriter: ConfigWriter[ModuleSource] =
    ConfigWriter[String].contramap[ModuleSource] {
      case DockerHubSource(name) => s"dockerhub:$name"
    }
}

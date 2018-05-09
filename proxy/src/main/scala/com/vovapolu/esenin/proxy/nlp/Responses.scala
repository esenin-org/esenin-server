package com.vovapolu.esenin.proxy.nlp

import cats.effect.IO
import io.circe.generic.auto._
import io.circe.generic.extras._
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe._

object Responses {
  implicit val circleConfig: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  @ConfiguredJsonCodec final case class PosPart(word: String,
                                                label: String,
                                                breakLevel: Int,
                                                category: String,
                                                head: Int,
                                                tag: String)

  final case class PosResponse(input: String, output: Seq[PosPart])

  implicit val posResponseEncoder: EntityEncoder[IO, PosResponse] =
    jsonEncoderOf[IO, PosResponse]
  implicit val posResponseDecoder: EntityDecoder[IO, PosResponse] =
    jsonOf[IO, PosResponse]
}

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
  final case class TmFitResponse(id: String)
  final case class TmTopicsResponse(topics: Seq[Double])

  implicit val posResponseEncoder: EntityEncoder[IO, PosResponse] =
    jsonEncoderOf[IO, PosResponse]
  implicit val posResponseDecoder: EntityDecoder[IO, PosResponse] =
    jsonOf[IO, PosResponse]

  implicit val tmTopicsResponseEncoder: EntityEncoder[IO, TmTopicsResponse] =
    jsonEncoderOf[IO, TmTopicsResponse]
  implicit val tmTopicsResponseDecoder: EntityDecoder[IO, TmTopicsResponse] =
    jsonOf[IO, TmTopicsResponse]

  implicit val tmFitResponseEncoder: EntityEncoder[IO, TmFitResponse] =
    jsonEncoderOf[IO, TmFitResponse]
  implicit val tmFitResponseDecoder: EntityDecoder[IO, TmFitResponse] =
    jsonOf[IO, TmFitResponse]
}

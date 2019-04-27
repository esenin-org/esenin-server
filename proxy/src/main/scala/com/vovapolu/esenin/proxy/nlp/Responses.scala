package com.vovapolu.esenin.proxy.nlp

import cats.effect.IO
import io.circe.generic.extras.auto._
import io.circe.generic.extras._
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe._

object Responses {
  implicit val circleConfig: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  final case class DependencyTreeNode(label: String, parent: Int)

  final case class TokenizeResponse(tokens: List[String])
  final case class DependencyTreeResponse(nodes: List[DependencyTreeNode])
  final case class PosResponse(pos: List[String])
  final case class TmFitResponse(id: String)
  final case class TmTopicsResponse(topics: List[Double])

  implicit val tokenizeResponseEncoder: EntityEncoder[IO, TokenizeResponse] =
    jsonEncoderOf[IO, TokenizeResponse]
  implicit val tokenizeResponseDecoder: EntityDecoder[IO, TokenizeResponse] =
    jsonOf[IO, TokenizeResponse]

  implicit val dependencyTreeResponseEncoder
    : EntityEncoder[IO, DependencyTreeResponse] =
    jsonEncoderOf[IO, DependencyTreeResponse]
  implicit val dependencyTreeResponseDecoder
    : EntityDecoder[IO, DependencyTreeResponse] =
    jsonOf[IO, DependencyTreeResponse]

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

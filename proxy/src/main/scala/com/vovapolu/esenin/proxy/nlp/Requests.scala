package com.vovapolu.esenin.proxy.nlp

import cats.effect.IO
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe._

object Requests {
  final case class PosRequest(string: String)
  final case class TmFitRequest(terms: Seq[Seq[String]], topics: Int)
  final case class TmTopicsRequest(term: String, id: String)

  implicit val posRequestDecoder: EntityDecoder[IO, PosRequest] =
    jsonOf[IO, PosRequest]
  implicit val tmFitRequestDecoder: EntityDecoder[IO, TmFitRequest] =
    jsonOf[IO, TmFitRequest]
  implicit val tmTopicsRequestDecoder: EntityDecoder[IO, TmTopicsRequest] =
    jsonOf[IO, TmTopicsRequest]
}

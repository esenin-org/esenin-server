package com.vovapolu.esenin.proxy.nlp

import cats.effect.IO
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe._

object Requests {
  case class PosRequest(string: String)

  implicit val posRequestDecoder: EntityDecoder[IO, PosRequest] =
    jsonOf[IO, PosRequest]
}

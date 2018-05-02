package com.vovapolu.esenin.proxy

import cats.effect.IO
import com.vovapolu.esenin.proxy.nlp.Requests._
import com.vovapolu.esenin.proxy.nlp.Responses._
import com.vovapolu.esenin.proxy.nlp.Requests.PosRequest
import com.vovapolu.esenin.proxy.nlp.Responses.PosResponse
import fs2.StreamApp
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.blaze._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object EseninProxy extends StreamApp[IO] with Http4sDsl[IO] with Http4sClientDsl[IO] {


  val client = Http1Client[IO]().unsafeRunSync()

  val service = HttpService[IO] {
    case req @ POST -> Root / "api" / "pos" =>
      for {
        posRequst <- req.as[PosRequest]
        proxyRequst = POST(uri("http://localhost:9000/api/syntaxnet"), posRequst.asJson)
        posResponse <- client.expect(proxyRequst)(jsonOf[IO, PosResponse])
        response <- Ok(posResponse)
      } yield response
  }

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(service, "/")
      .serve
}

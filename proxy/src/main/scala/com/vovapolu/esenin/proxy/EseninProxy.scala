package com.vovapolu.esenin.proxy

import java.nio.file.Paths

import cats.effect.IO
import com.vovapolu.esenin.config
import com.vovapolu.esenin.proxy.nlp.Requests._
import com.vovapolu.esenin.proxy.nlp.Responses._
import com.vovapolu.esenin.proxy.nlp.Requests.PosRequest
import com.vovapolu.esenin.proxy.nlp.Responses.PosResponse
import fs2.StreamApp
import fs2.StreamApp.ExitCode
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.Uri.{Authority, RegName}
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.blaze._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object EseninProxy extends StreamApp[IO] with Http4sDsl[IO] with Http4sClientDsl[IO] {

  val clientIO: IO[Client[IO]] = Http1Client[IO]()

  def service(conf: config.ModulesConfig): HttpService[IO] = {
    def findAndReroute[Req, Res](req: Request[IO], nlpFunc: config.NlpFunc, apiMethod: String)(
        implicit decoder: EntityDecoder[IO, Req],
        e: Encoder[Req],
        d: Decoder[Res],
        encoder: EntityEncoder[IO, Res]) = {
      val module = conf.modules.find(_.nlpFunc == nlpFunc)
      module match {
        case Some(m) =>
          for {
            client <- clientIO
            request <- req.as[Req]
            uri <- IO.fromEither(Uri.fromString(s"http://${m.name}:9000/api/$apiMethod"))
            proxyRequst = POST(uri, request.asJson)
            posResponse <- client.expect(proxyRequst)(jsonOf[IO, Res])
            response <- Ok(posResponse)
          } yield response
        case None => BadRequest("Wrong NLP function")
      }
    }

    HttpService[IO] {
      case req @ POST -> Root / "nlp" / config.Token.name =>
        findAndReroute[TokenizeRequest, TokenizeResponse](req, config.Token, "token")
      case req @ POST -> Root / "nlp" / config.DTree.name =>
        findAndReroute[DependencyTreeRequest, DependencyTreeResponse](req, config.DTree, "dtree")
      case req @ POST -> Root / "nlp" / config.POS.name =>
        findAndReroute[PosRequest, PosResponse](req, config.POS, "pos")
      case req @ POST -> Root / "nlp" / config.TM.name / "fit" =>
        findAndReroute[TmFitRequest, TmFitResponse](req, config.TM, "fit")
      case req @ POST -> Root / "nlp" / config.TM.name / "topics" =>
        findAndReroute[TmTopicsRequest, TmTopicsResponse](req, config.TM, "topics")
    }
  }

  def stream(args: List[String], requestShutdown: IO[Unit]): fs2.Stream[IO, ExitCode] = {
    val confE = config.loadModules(
      Paths.get(s"/etc/${config.Constants.modulesConfFilename}")
    )

    confE match {
      case Left(err) =>
        fs2.Stream.eval(IO[ExitCode] {
          println(s"Errors occured during config loading: ${err.mkString(", ")}")
          ExitCode(1)
        })
      case Right(conf) =>
        BlazeBuilder[IO]
          .bindHttp(9000, "0.0.0.0")
          .mountService(service(conf), "/")
          .serve
    }
  }
}

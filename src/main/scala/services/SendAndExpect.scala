package com.dallinhuff.loadbalancer.services

import com.dallinhuff.loadbalancer.http.{HttpClient, ServerHealthStatus}
import org.http4s.client.UnexpectedStatus
import org.http4s.{Request, Uri}
import cats.syntax.option.*
import cats.effect.IO
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*
import cats.syntax.applicative.*

import scala.concurrent.duration.DurationInt

trait SendAndExpect[A]:
  def apply(uri: Uri): IO[A]

object SendAndExpect:
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def toBackend(httpClient: HttpClient, req: Request[IO]): SendAndExpect[String] =
    new SendAndExpect[String]:
      override def apply(uri: Uri): IO[String] =
        info"[LOAD-BALANCER] sending request to $uri" *> httpClient
          .sendAndReceive(uri, req.some)
          .handleErrorWith:
            case UnexpectedStatus(org.http4s.Status.NotFound, _, _) =>
              "resource was not found"
                .pure[IO]
                .flatTap(msg => warn"$msg")
            case _ =>
              s"server with uri: $uri is dead"
                .pure[IO]
                .flatTap(msg => warn"$msg")

  def toHealthCheck(httpClient: HttpClient): SendAndExpect[ServerHealthStatus] =
    new SendAndExpect[ServerHealthStatus]:
      override def apply(uri: Uri): IO[ServerHealthStatus] =
        info"[HEALTH-CHECK] checking $uri health" *>
          httpClient
            .sendAndReceive(uri, none)
            .as(ServerHealthStatus.Alive)
            .flatTap(_ => info"$uri is alive")
            .timeout(5.seconds)
            .handleErrorWith(_ => warn"$uri is dead" *> ServerHealthStatus.Dead.pure[IO])

  val BackendSuccessTest: SendAndExpect[String] = _ => "Success".pure[IO]


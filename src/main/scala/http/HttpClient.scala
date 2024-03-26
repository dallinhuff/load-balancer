package com.dallinhuff.loadbalancer.http

import cats.effect.IO
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.{Request, Uri}

import scala.concurrent.duration.DurationInt

trait HttpClient:
  def sendAndReceive(uri: Uri, requestOpt: Option[Request[IO]] = None): IO[String]

object HttpClient:
  def of(client: Client[IO]): HttpClient = new HttpClient:
    override def sendAndReceive(uri: Uri, requestOpt: Option[Request[IO]] = None): IO[String] =
      requestOpt match
        case Some(request) => client.expect[String](request.withUri(uri))
        case None => client.expect[String](uri)


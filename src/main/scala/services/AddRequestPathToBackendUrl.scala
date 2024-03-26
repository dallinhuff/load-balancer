package com.dallinhuff.loadbalancer.services

import cats.effect.IO
import org.http4s.Request

trait AddRequestToBackendUrl:
  def apply(backendUrl: String, request: Request[IO]): String

object AddRequestToBackendUrl:
  object Impl extends AddRequestToBackendUrl:
    override def apply(backendUrl: String, request: Request[IO]): String =
      val requestPath =
        request.uri.path.renderString.dropWhile(_ != '/')
      backendUrl ++ requestPath


package com.dallinhuff.loadbalancer.services

import com.dallinhuff.loadbalancer.domain.*
import com.dallinhuff.loadbalancer.domain.UrlsRef.*
import com.dallinhuff.loadbalancer.services.RoundRobin.BackendsRoundRobin
import cats.effect.IO
import org.http4s.Uri.Path.Segment
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Request}

object LoadBalancer:
  def from(
    backends: Backends,
    sendAndExpectResponse: Request[IO] => SendAndExpect[String],
    parseUri: ParseUri,
    addRequestPathToBackendUrl: AddRequestToBackendUrl,
    backendsRoundRobin: BackendsRoundRobin,
  ): HttpRoutes[IO] =
    val dsl = new Http4sDsl[IO] {}
    import dsl.*
    HttpRoutes.of[IO]: request =>
      backendsRoundRobin(backends).flatMap:
        _.fold(Ok("All backends are inactive")): backendUrl =>
          val url = addRequestPathToBackendUrl(backendUrl.toString, request)
          for
            uri      <- IO.fromEither(parseUri(url))
            response <- sendAndExpectResponse(request)(uri)
            result   <- Ok(response)
          yield result


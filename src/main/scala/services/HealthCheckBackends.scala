package com.dallinhuff.loadbalancer.services

import com.dallinhuff.loadbalancer.domain.*
import com.dallinhuff.loadbalancer.domain.UrlsRef.*
import com.dallinhuff.loadbalancer.http.ServerHealthStatus
import com.dallinhuff.loadbalancer.services.RoundRobin.HealthChecksRoundRobin
import cats.effect.IO

import scala.concurrent.duration.DurationLong

object HealthCheckBackends:

  def periodically(
    healthChecks: HealthChecks,
    backends: Backends,
    parseUri: ParseUri,
    updateBackendsAndGet: UpdateBackendsAndGet,
    healthChecksRoundRobin: HealthChecksRoundRobin,
    sendAndExpectStatus: SendAndExpect[ServerHealthStatus],
    healthCheckInterval: HealthCheckInterval,
  ): IO[Unit] =
    checkHealthAndUpdateBackends(
      healthChecks,
      backends,
      parseUri,
      updateBackendsAndGet,
      healthChecksRoundRobin,
      sendAndExpectStatus,
    ).flatMap(_ => IO.sleep(healthCheckInterval.toLong.seconds)).foreverM

  private[services] def checkHealthAndUpdateBackends(
    healthChecks: HealthChecks,
    backends: Backends,
    parseUri: ParseUri,
    updateBackendsAndGet: UpdateBackendsAndGet,
    healthChecksRoundRobin: HealthChecksRoundRobin,
    sendAndExpectStatus: SendAndExpect[ServerHealthStatus],
  ): IO[Urls] =
    for
      currentUrl <- healthChecksRoundRobin(healthChecks)
      uri        <- IO.fromEither(parseUri(currentUrl.toString))
      status     <- sendAndExpectStatus(uri)
      updated    <- updateBackendsAndGet(backends, currentUrl, status)
    yield updated


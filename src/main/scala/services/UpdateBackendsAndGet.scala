package com.dallinhuff.loadbalancer.services

import com.dallinhuff.loadbalancer.domain.{*, given}
import com.dallinhuff.loadbalancer.domain.UrlsRef.Backends
import com.dallinhuff.loadbalancer.http.ServerHealthStatus
import cats.effect.IO

trait UpdateBackendsAndGet:
  def apply(backends: Backends, url: Url, status: ServerHealthStatus): IO[Urls]

object UpdateBackendsAndGet: 
  object Impl extends UpdateBackendsAndGet:
    override def apply(backends: Backends, url: Url, status: ServerHealthStatus): IO[Urls] = 
      backends.urls.updateAndGet: urls =>
        status match
          case ServerHealthStatus.Alive => urls.add(url)
          case ServerHealthStatus.Dead  => urls.remove(url)


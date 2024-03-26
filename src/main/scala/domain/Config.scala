package com.dallinhuff.loadbalancer.domain

import pureconfig.{ConfigReader, *}
import pureconfig.generic.derivation.default.*

final case class Config(
    port: Int,
    host: String,
    backends: Urls,
    healthCheckInterval: HealthCheckInterval
) derives ConfigReader

object Config:
  given ConfigReader[Url] =
    ConfigReader[String].map(Url.apply)
  given ConfigReader[Urls] =
    ConfigReader[Vector[Url]].map(Urls(_*))
  given ConfigReader[HealthCheckInterval] =
    ConfigReader[Long].map(HealthCheckInterval(_))


package com.dallinhuff.loadbalancer.domain

opaque type HealthCheckInterval = Long

object HealthCheckInterval:
  inline def apply(interval: Long): HealthCheckInterval = interval

extension (interval: HealthCheckInterval)
  def toLong: Long = interval


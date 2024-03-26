package com.dallinhuff.loadbalancer.services

import cats.Id
import cats.effect.IO
import com.dallinhuff.loadbalancer.domain.{*, given}
import cats.syntax.option.*

import scala.util.Try

trait RoundRobin[F[_]]:
  def apply(ref: UrlsRef): IO[F[Url]]

object RoundRobin:
  type BackendsRoundRobin = RoundRobin[Option]
  type HealthChecksRoundRobin = RoundRobin[Id]

  def forBackends: BackendsRoundRobin = new BackendsRoundRobin:
    override def apply(ref: UrlsRef): IO[Option[Url]] =
      ref.urls.getAndUpdate(_.rotated).map(_.currentOption)

  def forHealthChecks: HealthChecksRoundRobin = new HealthChecksRoundRobin:
    override def apply(ref: UrlsRef): IO[Id[Url]] =
      ref.urls.getAndUpdate(_.rotated).map(_.currentUnsafe)

  val TestId: RoundRobin[Id] = _ => IO.pure(Url("localhost:8081"))
  val LocalHost8081: RoundRobin[Option] = _ => IO.pure(Some(Url("localhost:8081")))


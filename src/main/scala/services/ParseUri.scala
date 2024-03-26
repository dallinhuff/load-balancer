package com.dallinhuff.loadbalancer.services

import com.dallinhuff.loadbalancer.errors.parsing.InvalidUri
import cats.syntax.either.*
import org.http4s.Uri

trait ParseUri:
  def apply(uri: String): Either[InvalidUri, Uri]

object ParseUri:
  object Impl extends ParseUri:
    override def apply(uri: String): Either[InvalidUri, Uri] =
      Uri.fromString(uri).leftMap(_ => InvalidUri(uri))


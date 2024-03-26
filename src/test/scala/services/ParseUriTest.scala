package com.dallinhuff.loadbalancer.services

import com.dallinhuff.loadbalancer.errors.parsing.InvalidUri
import munit.FunSuite
import org.http4s.Uri
import cats.syntax.either.*

class ParseUriTest extends FunSuite:
  val parseUri = ParseUri.Impl

  test("parsing valid uri returns Right(...)"):
    val uri = "0.0.0.0/8080"
    val obtained = parseUri(uri)

    assertEquals(obtained, Uri.unsafeFromString(uri).asRight)

  test("parsing invalid uri returns Left(...)"):
    val uri = "definitely not a good uri"
    val obtained = parseUri(uri)

    assertEquals(obtained, InvalidUri(uri).asLeft)


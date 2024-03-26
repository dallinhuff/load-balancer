package com.dallinhuff.loadbalancer.services

import cats.effect.IO
import munit.FunSuite
import org.http4s.{EntityBody, *}

class AddRequestToBackendUrlTest extends FunSuite:
  val impl = AddRequestToBackendUrl.Impl
  val backendUrl = "http://localhost:8082"

  test("add /items/1 to backendUrl"):
    val request = Request[IO](uri = Uri.unsafeFromString("localhost:8080/items/1"))
    val obtained = impl(backendUrl, request)
    val expected = s"$backendUrl/items/1"

    assertEquals(obtained, expected)

  test("return backendUrl when no following path"):
    val request = Request[IO](uri = Uri.unsafeFromString("localhost:8080"))
    val obtained = impl(backendUrl, request)
    val expected = backendUrl

    assertEquals(obtained, expected)


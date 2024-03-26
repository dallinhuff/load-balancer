package com.dallinhuff.loadbalancer.services

import com.dallinhuff.loadbalancer.http.{HttpClient, ServerHealthStatus}
import cats.effect.IO
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.{Request, Uri}
import munit.{CatsEffectSuite, FunSuite}

import scala.concurrent.duration.DurationInt

class SendAndExpectTest extends CatsEffectSuite:
  val localhost8080 = "localhost:8080"
  val backend = Uri.fromString(localhost8080).toOption.get
  val emptyRequest= Request[IO]()

  val Hello: HttpClient = (_, _) => IO.pure("Hello")
  val TestTimeToFailure: HttpClient      = (_, _) => IO.sleep(6.seconds).as("")
  val RuntimeException: HttpClient = (_, _) => IO.raiseError(new RuntimeException("Server is dead"))
  val BackendResourceNotFound: HttpClient = (_, _) =>
    IO.raiseError:
      UnexpectedStatus(
        org.http4s.Status.NotFound,
        org.http4s.Method.GET,
        Uri.unsafeFromString("localhost:8081")
      )

  test("toBackend [success]"):
    val sendAndExpect = SendAndExpect.toBackend(Hello, emptyRequest)
    val obtained = sendAndExpect(backend)

    assertIO(obtained, "Hello")

  test("toBackend [failure]"):
    val sendAndExpect = SendAndExpect.toBackend(RuntimeException, emptyRequest)
    val obtained = sendAndExpect(backend)

    assertIO(obtained, s"server with uri: $localhost8080 is dead")

  test("toHealthCheck [alive]"):
    val sendAndExpect = SendAndExpect.toHealthCheck(Hello)
    val obtained = sendAndExpect(backend)

    assertIO(obtained, ServerHealthStatus.Alive)

  test("toHealthCheck [dead due to timeout]"):
    val sendAndExpect = SendAndExpect.toHealthCheck(TestTimeToFailure)
    val obtained = sendAndExpect(backend)
    
    assertIO(obtained, ServerHealthStatus.Dead)

  test("toHealthCheck [dead due to exception]"):
    val sendAndExpect = SendAndExpect.toHealthCheck(RuntimeException)
    val obtained = sendAndExpect(backend)

    assertIO(obtained, ServerHealthStatus.Dead)


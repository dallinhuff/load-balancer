package com.dallinhuff.loadbalancer.services

import com.dallinhuff.loadbalancer.domain.*
import com.dallinhuff.loadbalancer.domain.UrlsRef.*
import com.dallinhuff.loadbalancer.http.HttpClient
import cats.effect.IO
import munit.{CatsEffectSuite, FunSuite}
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.Uri

import scala.concurrent.duration.DurationInt

class HealthCheckBackendsTest extends CatsEffectSuite {

  val Hello: HttpClient = (_, _) => IO.pure("Hello")
  val TestTimeToFailure: HttpClient = (_, _) => IO.sleep(6.seconds).as("")
  val RuntimeException: HttpClient = (_, _) => IO.raiseError(new RuntimeException("Server is dead"))
  val BackendResourceNotFound: HttpClient = (_, _) =>
    IO.raiseError:
      UnexpectedStatus(
        org.http4s.Status.NotFound,
        org.http4s.Method.GET,
        Uri.unsafeFromString("localhost:8081")
      )

  test("add backend url to the Backends as soon as health check returns success") {
    val healthChecks = Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply)*)
    val obtained = for
      backends     <- IO.ref(Urls(Url("localhost:8082")))
      healthChecks <- IO.ref(healthChecks)
      result       <- HealthCheckBackends.checkHealthAndUpdateBackends(
        HealthChecks(healthChecks),
        Backends(backends),
        ParseUri.Impl,
        UpdateBackendsAndGet.Impl,
        RoundRobin.forHealthChecks,
        SendAndExpect.toHealthCheck(Hello)
      )
    yield result

    assertIO(obtained, Urls(Vector("localhost:8082", "localhost:8081").map(Url.apply)*))
  }

  test("remove backend url from the Backends as soon as health check returns failure") {
    val urls = Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply)*)
    val obtained = for
      backends     <- IO.ref(urls)
      healthChecks <- IO.ref(urls)
      result       <- HealthCheckBackends.checkHealthAndUpdateBackends(
        HealthChecks(healthChecks),
        Backends(backends),
        ParseUri.Impl,
        UpdateBackendsAndGet.Impl,
        RoundRobin.forHealthChecks,
        SendAndExpect.toHealthCheck(TestTimeToFailure)
      )
    yield result

    assertIO(obtained, Urls(Vector("localhost:8082").map(Url.apply)*))
  }
}

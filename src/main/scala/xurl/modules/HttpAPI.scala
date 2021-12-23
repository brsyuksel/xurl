package xurl.modules

import scala.concurrent.duration._

import xurl.http.{ HealthRoutes, RedirectRoutes, UrlRoutes }

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.implicits._
import org.http4s.metrics.prometheus.{ Prometheus, PrometheusExportService }
import org.http4s.server.Router
import org.http4s.server.middleware._

abstract class HttpAPI[F[_]: Async] private (services: Services[F]) {
  private val urlRoutes      = new UrlRoutes[F](services.urls, services.shortener).routes
  private val redirectRoutes = new RedirectRoutes[F](services.routing).routes
  private val healthRoutes   = new HealthRoutes[F](services.healthCheck).routes

  private val apiRoutes: HttpRoutes[F] = Router(
    "/api/v1"  -> CORS(AutoSlash(urlRoutes)),
    "/_health" -> CORS(AutoSlash(healthRoutes))
  )

  private val allRoutes = Timeout(60.seconds)(apiRoutes <+> redirectRoutes)

  private def reqLogger: HttpApp[F] => HttpApp[F] = { http =>
    RequestLogger.httpApp(true, true)(http)
  }

  lazy val httpApp: Resource[F, HttpApp[F]] =
    for {
      service <- PrometheusExportService.build[F]
      metrics <- Prometheus.metricsOps[F](service.collectorRegistry, "xurl_http")
      router = Router(
        "/"            -> Metrics[F](metrics)(allRoutes),
        "/_prometheus" -> service.routes
      )
      app = reqLogger(router.orNotFound)
    } yield app
}

object HttpAPI {
  def make[F[_]: Async](services: Services[F]): HttpAPI[F] =
    new HttpAPI[F](services) {}
}

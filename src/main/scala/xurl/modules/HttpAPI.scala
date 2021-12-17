package xurl.modules

import scala.concurrent.duration._

import xurl.http.{ RedirectRoutes, UrlRoutes }

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware._

sealed abstract class HttpAPI[F[_]: Async] private (services: Services[F]) {
  private val urlRoutes      = new UrlRoutes[F](services.urls, services.shortener).routes
  private val redirectRoutes = new RedirectRoutes[F](services.routing).routes

  private val apiRoute: HttpRoutes[F] = Router(
    "/api/v1" -> CORS(AutoSlash(urlRoutes))
  )

  private val routes = Timeout(60.seconds)(apiRoute <+> redirectRoutes)

  private def reqLogger: HttpApp[F] => HttpApp[F] = { http =>
    RequestLogger.httpApp(true, true)(http)
  }

  val httpApp: HttpApp[F] = reqLogger(routes.orNotFound)
}

object HttpAPI {
  def make[F[_]: Async](services: Services[F]): HttpAPI[F] =
    new HttpAPI[F](services) {}
}

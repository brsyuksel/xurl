package xurl.http

import xurl.services.Routing
import xurl.url.model.Code

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._

final case class RedirectRoutes[F[_]: Concurrent](routing: Routing[F]) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / code =>
    routing
      .resolve(Code(code))
      .flatMap { addr =>
        MovedPermanently(Location(Uri.unsafeFromString(addr.value)))
      }
      .recoverWith { case Routing.RouteNotFoundError =>
        NotFound()
      }
  }
}

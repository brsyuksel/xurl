package xurl.http

import cats._
import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.implicits._
import org.http4s.headers._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

import xurl.services.Routing
import xurl.url.model.{ Address, Code }

final case class RedirectRoutes[F[_]: Concurrent: MonadThrow](routing: Routing[F]) extends Http4sDsl[F] {

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

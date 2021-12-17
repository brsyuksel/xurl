package xurl.http

import xurl.http.params._
import xurl.services.Shortener
import xurl.url.Urls
import xurl.url.model.Code

import cats._
import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final case class UrlRoutes[F[_]: Concurrent: MonadThrow](urls: Urls[F], shortener: Shortener[F]) extends Http4sDsl[F] {
  private[http] val prefix = "/urls"

  object OffsetMatcher extends OptionalQueryParamDecoderMatcher[Int]("offset")
  object LimitMatcher  extends OptionalQueryParamDecoderMatcher[Int]("limit")

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? OffsetMatcher(offset) +& LimitMatcher(limit) =>
      Ok(urls.list(offset.getOrElse(0), limit.getOrElse(10)))

    case GET -> Root / code =>
      urls.get(Code(code)).flatMap {
        case None    => NotFound()
        case Some(u) => Ok(u)
      }

    case req @ POST -> Root =>
      req.decode[AddressParam] { addr =>
        shortener
          .shorten(addr.toAddress)
          .flatMap(Created(_))
          .recoverWith { case Shortener.NotStoredError =>
            InternalServerError()
          }
      }

  }

  val routes: HttpRoutes[F] = Router(
    prefix -> httpRoutes
  )
}

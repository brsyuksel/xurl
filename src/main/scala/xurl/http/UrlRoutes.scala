package xurl.http

import xurl.http.params._
import xurl.services.Shortener
import xurl.url.Urls
import xurl.url.model.Code

import _root_.play.api.libs.json._
import cats._
import cats.data.EitherT
import cats.effect._
import cats.implicits._
import fs2.Chunk
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final case class UrlRoutes[F[_]: Concurrent: MonadThrow](urls: Urls[F], shortener: Shortener[F]) extends Http4sDsl[F] {
  private[http] val prefix = "/urls"

  object OffsetMatcher extends OptionalQueryParamDecoderMatcher[Int]("offset")
  object LimitMatcher  extends OptionalQueryParamDecoderMatcher[Int]("limit")

  // Manual EntityEncoder/EntityDecoder instances for play-json
  implicit def playJsonEncoder[A](implicit writes: Writes[A]): EntityEncoder[F, A] =
    EntityEncoder[F, Chunk[Byte]]
      .contramap[A] { a =>
        val json  = Json.toJson(a)
        val bytes = Json.stringify(json).getBytes(java.nio.charset.StandardCharsets.UTF_8)
        Chunk.array(bytes)
      }
      .withContentType(org.http4s.headers.`Content-Type`(MediaType.application.json))

  implicit def playJsonDecoder[A](implicit reads: Reads[A]): EntityDecoder[F, A] =
    new EntityDecoder[F, A] {
      override def consumes: Set[MediaRange] = Set(MediaType.application.json)

      override def decode(m: Media[F], strict: Boolean): DecodeResult[F, A] =
        EitherT {
          m.bodyText.compile.string.map { str =>
            Json.parse(str).validate[A] match {
              case JsSuccess(value, _) => Right(value)
              case JsError(errors)     => Left(InvalidMessageBodyFailure(s"Invalid JSON: $errors"))
            }
          }
        }
    }

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

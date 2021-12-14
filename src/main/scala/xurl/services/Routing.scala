package xurl.services

import scala.util.control.NoStackTrace

import cats.MonadThrow
import cats.implicits._

import xurl.url.model.{ Address, Code, Url }
import xurl.url.Urls
import xurl.caching.Cache

trait Routing[F[_]] {
  def resolve(code: Code): F[Address]
}

object Routing {
  def make[F[_]: MonadThrow](urls: Urls[F], cache: Cache[F]): Routing[F] =
    new Routing[F] {
      private def get(code: Code): F[Option[String]] =
        cache
          .through(code.value, urls.get(code).map(_.map(_.address.value)))
      def resolve(code: Code): F[Address] =
        (get(code) >>= { o =>
          MonadThrow[F].fromOption(o.map(Address(_)), RouteNotFoundError)
        }) <* urls.hit(code)
    }

  case class RoutingError(message: String) extends NoStackTrace
  object RouteNotFoundError                extends RoutingError("short url code is not valid")
}

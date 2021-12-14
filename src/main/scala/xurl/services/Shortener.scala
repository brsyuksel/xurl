package xurl.services

import scala.util.control.NoStackTrace

import cats.MonadThrow
import cats.implicits._

import xurl.url.Urls
import xurl.url.model.{ Address, Code, Url }

trait Shortener[F[_]] {
  def shorten(address: Address): F[Url]
}

object Shortener {
  def make[F[_]: MonadThrow](serial: SerialCode[F], urls: Urls[F]): Shortener[F] =
    new Shortener[F] {
      def shorten(address: Address): F[Url] =
        for {
          codeStr <- serial.next
          u = Url(Code(codeStr), address)
          code   <- urls.create(u)
          objOpt <- urls.get(code)
          obj    <- MonadThrow[F].fromOption(objOpt, NotStoredError)
        } yield obj
    }

  case class ShortenerError(message: String) extends NoStackTrace
  object NotStoredError                      extends ShortenerError("url couldn't have been stored")
}

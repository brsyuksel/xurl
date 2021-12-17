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
  def make[F[_]: MonadThrow](serialCode: SerialCode[F], urls: Urls[F]): Shortener[F] =
    addr =>
      for {
        codeStr <- serialCode.next
        u = Url(Code(codeStr), addr)
        code   <- urls.create(u)
        objOpt <- urls.get(code)
        obj    <- MonadThrow[F].fromOption(objOpt, NotStoredError)
      } yield obj

  case class ShortenerError(message: String) extends NoStackTrace
  object NotStoredError                      extends ShortenerError("url couldn't have been stored")
}

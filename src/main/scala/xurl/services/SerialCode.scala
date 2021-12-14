package xurl.services

import cats.FlatMap
import cats.implicits._

import xurl.serial.{ BaseN, Serial }

trait SerialCode[F[_]] {
  def next: F[String]
}

object SerialCode {
  def make[F[_]: FlatMap](serial: Serial[F], baseN: BaseN[F]): SerialCode[F] =
    new SerialCode[F] {
      def next: F[String] =
        serial.get >>= baseN.encode
    }
}

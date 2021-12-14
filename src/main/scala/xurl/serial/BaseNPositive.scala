package xurl.serial

import scala.annotation.tailrec

import cats.{ ApplicativeThrow, FlatMap }
import cats.implicits._

final case class BaseNPositive[F[_]: ApplicativeThrow: FlatMap](letters: String) extends BaseN[F] {
  import BaseNPositive._

  def encode(n: Long): F[String] =
    (n >= 0).pure[F].ifM(ten2N(n).pure[F], NumberIsNotPositive.raiseError[F, String])

  private lazy val base: Long = letters.length
  private def ten2N(n: Long): String = {
    @tailrec
    def aux(r: Long, c: List[Int] = Nil): List[Int] =
      (r / base) match {
        case 0 => r.toInt :: c
        case q => aux(q, (r % base).toInt :: c)
      }

    aux(n).map(letters).mkString
  }
}

object BaseNPositive {
  object NumberIsNotPositive extends model.SerialError("number is not positive")
}

package xurl.serial

import cats.effect._
import cats.implicits._
import skunk._
import skunk.codec.numeric.int8
import skunk.implicits._

final case class SkunkSerial[F[_]: MonadCancelThrow](pg: Resource[F, Session[F]]) extends Serial[F] {
  import SkunkSerial._

  override def get: F[Long] =
    pg.use(_.unique(SQL.nextVal))
}

object SkunkSerial {
  private case object SQL {
    val nextVal: Query[Void, Long] =
      sql"""
      SELECT nextval('serial_int');
      """.query(int8)
  }
}

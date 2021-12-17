package xurl.health

import scala.concurrent.duration._

import cats.effect._
import cats.effect.implicits._
import cats.syntax.all._
import skunk._
import skunk.codec.boolean.bool
import skunk.implicits._

final case class SkunkHealth[F[_]: Temporal](pg: Resource[F, Session[F]]) extends Health[F] {
  import SkunkHealth._

  override def ok: F[Boolean] =
    pg
      .use(_.unique(SQL.selectTrue))
      .timeout(1.second)
      .orElse(false.pure[F])
}

object SkunkHealth {
  private case object SQL {
    val selectTrue: Query[Void, Boolean] =
      sql"""
      SELECT TRUE
      """.query(bool)
  }
}

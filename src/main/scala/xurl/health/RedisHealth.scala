package xurl.health

import scala.concurrent.duration._

import cats.effect._
import cats.effect.implicits._
import cats.syntax.all._
import dev.profunktor.redis4cats.RedisCommands

final case class RedisHealth[F[_]: Temporal](
    redis: RedisCommands[F, String, String]
) extends Health[F] {

  override def ok: F[Boolean] =
    redis.ping
      .map(_.nonEmpty)
      .timeout(1.second)
      .orElse(false.pure[F])

}

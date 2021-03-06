package xurl.resources

import xurl.config.AppConfig

import cats.effect._
import cats.effect.std.Console
import cats.implicits._
import dev.profunktor.redis4cats.effect.MkRedis
import dev.profunktor.redis4cats.{ Redis, RedisCommands }
import natchez.Trace.Implicits.noop
import skunk._

sealed abstract class AppResources[F[_]](
    val pg: Resource[F, Session[F]],
    val redis: RedisCommands[F, String, String]
)

object AppResources {
  def make[F[_]: Async: Console: MkRedis](conf: AppConfig): Resource[F, AppResources[F]] = {
    lazy val pgPool: SessionPool[F] =
      Session.pooled[F](
        host = conf.db.host,
        port = conf.db.port,
        user = conf.db.user,
        database = conf.db.database,
        password = conf.db.password,
        max = conf.db.connections
      )

    lazy val redis: Resource[F, RedisCommands[F, String, String]] =
      Redis[F].utf8(conf.cache.url)

    (pgPool, redis).parMapN(new AppResources[F](_, _) {})
  }
}

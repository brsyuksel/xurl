package xurl.caching

import scala.concurrent.duration.FiniteDuration

import cats.MonadThrow
import cats.syntax.all._
import dev.profunktor.redis4cats.RedisCommands

final case class RedisStringCache[F[_]: MonadThrow](
    redis: RedisCommands[F, String, String],
    expiration: FiniteDuration
) extends Cache[F] {
  private def set(key: String, value: String): F[Unit] =
    redis.set(key, value) <* redis.expire(key, expiresIn = expiration)
  private def get(key: String): F[Option[String]] =
    redis.get(key)
  override def through(key: String, fk: => F[Option[String]]): F[Option[String]] =
    get(key) >>= {
      case None =>
        fk.flatMap {
          case n @ None    => n.pure[F]
          case s @ Some(v) => set(key, v).as(s)
        }
      case s => s.pure[F]
    }
}

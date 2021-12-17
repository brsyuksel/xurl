package xurl.modules

import scala.concurrent.duration.FiniteDuration

import xurl.caching.RedisStringCache
import xurl.health._
import xurl.serial.{ BaseNPositive, SkunkSerial }
import xurl.services._
import xurl.url.{ SkunkUrls, Urls }

import cats.effect._
import dev.profunktor.redis4cats.RedisCommands
import skunk.Session

sealed abstract class Services[F[_]] private (
    val urls: Urls[F],
    val routing: Routing[F],
    val shortener: Shortener[F],
    val healthCheck: HealthCheck[F]
)

object Services {
  def make[F[_]: Async](
      pg: Resource[F, Session[F]],
      redis: RedisCommands[F, String, String],
      redisExp: FiniteDuration,
      baseNLetters: String
  ): Services[F] = {
    val skunkUrls  = new SkunkUrls[F](pg)
    val serial     = new SkunkSerial[F](pg)
    val redisCache = new RedisStringCache[F](redis, redisExp)

    val baseN      = new BaseNPositive[F](baseNLetters)
    val serialCode = SerialCode.make(serial, baseN)
    val shortener  = Shortener.make(serialCode, skunkUrls)

    val routing = Routing.make(skunkUrls, redisCache)

    val pgHealth    = new SkunkHealth[F](pg)
    val redisHealth = new RedisHealth[F](redis)
    val healthCheck = HealthCheck.make[F](pgHealth, redisHealth)

    new Services[F](skunkUrls, routing, shortener, healthCheck) {}
  }
}

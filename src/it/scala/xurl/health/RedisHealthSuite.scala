package xurl.health

import cats.effect._
import dev.profunktor.redis4cats.log4cats._
import dev.profunktor.redis4cats.{ Redis, RedisCommands }
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.noop.NoOpLogger
import weaver.IOSuite

object RedisHealthSuite extends IOSuite {
  implicit val logger: Logger[IO] = NoOpLogger[IO]

  type Res = RedisCommands[IO, String, String]

  override def sharedResource: Resource[IO, Res] =
    Redis[IO].utf8("redis://localhost")

  test("ok returns true") { redis =>
    val health = RedisHealth[IO](redis)
    health.ok.map(expect(_))
  }
}

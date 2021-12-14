package xurl.caching

import scala.concurrent.duration._

import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats.{ Redis, RedisCommands }
import dev.profunktor.redis4cats.log4cats._
import org.typelevel.log4cats.noop.NoOpLogger
import weaver.IOSuite

object RedisStringCacheSuite extends IOSuite {
  override def maxParallelism = 1
  
  implicit val logger = NoOpLogger[IO]

  type Res = RedisCommands[IO, String, String]

  override def sharedResource: Resource[IO, Res] =
    Redis[IO]
      .utf8("redis://localhost")
      .evalTap(_.flushAll)

  test("through:return") { redis =>
    val cache = new RedisStringCache[IO](redis, 10.seconds)
    Ref.of[IO, Boolean](false) >>= { r =>
      val t = for {
        _ <- redis.set("key1", "value1")
        fk = r.set(true).as(Some("value111"))
        v <- cache.through("key1", fk)
        called <- r.get
      } yield
        expect(v === Some("value1")) &&
        expect(called === false)
      
      t.guarantee(redis.flushAll)
    }
  }

  test("through:set-and-return") { redis =>
    val cache = new RedisStringCache[IO](redis, 10.seconds)
    Ref.of[IO, Boolean](false) >>= { r =>
      val fk = r.set(true).as(Some("set-value"))
      val nextFk = IO(Some("wont-be-called"))
      val t = for {
        v1 <- cache.through("new-key", fk)
        called <- r.get
        v2 <- cache.through("new-key", nextFk)
      } yield
        expect(v1 === Some("set-value")) &&
        expect(called) &&
        expect(v2 === Some("set-value"))
      
      t.guarantee(redis.flushAll)
    }
  }
}

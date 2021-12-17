package xurl.url

import xurl.serial.{ BaseNPositive, Serial }
import xurl.services.SerialCode
import xurl.url.model._

import cats.effect._
import cats.implicits._
import natchez.Trace.Implicits.noop
import skunk._
import skunk.implicits._
import weaver.IOSuite

object SkunkUrlsSuite extends IOSuite {

  override def maxParallelism: Int = 1
  type Res = Resource[IO, Session[IO]]

  override def sharedResource: Resource[IO, Res] =
    Session
      .pooled[IO](
        host = "localhost",
        port = 5432,
        user = "postgres",
        database = "xurl",
        password = Some("postgres"),
        max = 10
      )

  private def truncate(pg: Resource[IO, Session[IO]]): IO[Unit] =
    pg.use(_.execute(sql"TRUNCATE TABLE urls".command)).void

  test("create") { pg =>
    val urls = new SkunkUrls[IO](pg)

    val t = serialCode(0) >>= { s =>
      List.fill(10)(s.next).sequence.map(_.map(Code(_))) >>= { codes =>
        codes
          .map(c => Url(c, Address("create-testing")))
          .map(urls.create)
          .sequence
          .map(l => expect(l === codes))
      }
    }

    t.guarantee(truncate(pg))
  }

  test("create:duplicate") { pg =>
    val urls = new SkunkUrls[IO](pg)
    val obj  = Url(Code("duplicate"), Address("create:duplicate-testing"))
    val tt = for {
      _ <- urls.create(obj)
      _ <- urls.create(obj)
    } yield ()

    val t = tt.attempt.map(r => expect(r.isLeft))

    t.guarantee(truncate(pg))
  }

  test("get") { pg =>
    val urls = new SkunkUrls[IO](pg)
    val u    = Url(Code("get"), Address("get-testing"))
    val t = for {
      c   <- urls.create(u)
      obj <- urls.get(c)
    } yield expect(obj.nonEmpty) &&
      expect(obj.map(_.code) === Some(u.code)) &&
      expect(obj.map(_.address) === Some(u.address)) &&
      expect(obj.map(_.hit.value) === Some(0L)) &&
      expect(obj.flatMap(_.createdAt).nonEmpty)

    t.guarantee(truncate(pg))
  }

  test("get:notfound") { pg =>
    val urls = new SkunkUrls[IO](pg)
    urls.get(Code("non-existing-key")).map(o => expect(o.isEmpty))
  }

  test("list") { pg =>
    val urls = new SkunkUrls[IO](pg)
    val t = for {
      serial   <- serialCode(100)
      codesRes <- List.fill(10)(serial.next).sequence
      codes = codesRes.map(Code(_))
      created <- codes.map(c => Url(c, Address("list-testing"))).map(urls.create).sequence
      p1      <- urls.list(0, 5)
      p2      <- urls.list(5, 5)
    } yield expect(p1.size === 5) &&
      expect(p2.size === 5) &&
      expect(p1.map(_.code) === codes.take(5))

    t.guarantee(truncate(pg))
  }

  test("hit") { pg =>
    val urls = new SkunkUrls[IO](pg)
    val u    = Url(Code("hit"), Address("hit-testing"))
    val t = for {
      c   <- urls.create(u)
      _   <- urls.hit(c)
      _   <- urls.hit(c)
      obj <- urls.get(c)
    } yield expect(obj.nonEmpty) &&
      expect(obj.map(_.hit.value) === Some(2L))

    t.guarantee(truncate(pg))
  }

  private def makeIncrementingSerial(l: Long): IO[Serial[IO]] =
    Ref.of[IO, Long](l).map { r =>
      new Serial[IO] {
        def get: IO[Long] =
          for {
            _ <- r.update(_ + 1)
            v <- r.get
          } yield v
      }
    }

  private def makeBase15: BaseNPositive[IO] =
    new BaseNPositive[IO]("abc123def456ghi")

  private def serialCode(l: Long): IO[SerialCode[IO]] =
    makeIncrementingSerial(l).map(s => SerialCode.make[IO](s, makeBase15))
}

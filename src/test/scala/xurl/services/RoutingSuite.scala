package xurl.services

import cats.effect._
import cats.implicits._
import weaver.SimpleIOSuite

import xurl.url.model._
import xurl.url.Urls
import xurl.caching.Cache

object RoutingSuite extends SimpleIOSuite {
  test("resolve:from-cache") {
    Ref.of[IO, Boolean](false) >>= { r =>
      val urls    = mkDummyUrls(r)
      val cache   = mkDummyCache
      val routing = Routing.make(urls, cache)
      routing.resolve(Code("from-cache")).flatMap { a =>
        r.get.map { hit =>
          expect(a.value === "cache-addr") &&
          expect(hit)
        }
      }
    }
  }

  test("resolve:from-db") {
    Ref.of[IO, Boolean](false) >>= { r =>
      val urls    = mkDummyUrls(r)
      val cache   = mkDummyCache
      val routing = Routing.make(urls, cache)
      routing.resolve(Code("from-db")).flatMap { a =>
        r.get.map { hit =>
          expect(a.value === "db-addr") &&
          expect(hit)
        }
      }
    }
  }

  test("resolve:error") {
    Ref.of[IO, Boolean](false) >>= { r =>
      val urls    = mkDummyUrls(r)
      val cache   = mkDummyCache
      val routing = Routing.make(urls, cache)
      routing.resolve(Code("fail")).attempt.flatMap { e =>
        r.get.map { hit =>
          expect(e.isLeft) &&
          expect(e == Left(Routing.RouteNotFoundError))
          expect(hit === false)
        }
      }
    }
  }

  private def mkDummyUrls(ref: Ref[IO, Boolean]): Urls[IO] =
    new Urls[IO] {
      override def list(limit: Int, offset: Int): IO[List[Url]] =
        List.empty[Url].pure[IO]
      override def get(code: Code): IO[Option[Url]] =
        (code.value match {
          case "from-db" => Url(code, Address("db-addr")).some
          case _         => none[Url]
        }).pure[IO]
      override def create(url: Url): IO[Code] =
        url.code.pure[IO]
      override def hit(code: Code): IO[Unit] =
        ref.set(true)
    }

  private def mkDummyCache: Cache[IO] =
    new Cache[IO] {
      override def through(key: String, fk: => IO[Option[String]]): IO[Option[String]] =
        key match {
          case "from-cache" => IO(Some("cache-addr"))
          case _            => fk
        }
    }
}

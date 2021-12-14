package xurl.http

import java.time.{ LocalDateTime, ZoneOffset }

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.Method._
import org.http4s.client.dsl.io._
import org.http4s.syntax.literals._
import org.http4s.circe._
import io.circe._
import io.circe.literal._
import io.circe.syntax._
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers
import org.scalacheck.Gen

import xurl.services.Shortener
import xurl.url.Urls
import xurl.url.model._

object UrlRoutesSuite extends SimpleIOSuite with Checkers {
  val strGen: Gen[String] =
    Gen
      .chooseNum(3, 10)
      .flatMap { n =>
        Gen.buildableOfN[String, Char](n, Gen.alphaChar)
      }

  private val utcNow = LocalDateTime.now(ZoneOffset.UTC)
  val localDateTimeGen: Gen[LocalDateTime] =
    Gen
      .choose(
        utcNow.minusMonths(6).toEpochSecond(ZoneOffset.UTC),
        utcNow.toEpochSecond(ZoneOffset.UTC)
      )
      .map(n => LocalDateTime.ofEpochSecond(n, 0, ZoneOffset.UTC))

  val urlsGen: Gen[Url] =
    for {
      c <- strGen
      a <- strGen
      h <- Gen.posNum[Long]
      t <- localDateTimeGen
    } yield Url(Code(c), Address(a), Hit(h), Some(t))

  private def mkUrls(data: List[Url]): Urls[IO] =
    new Urls[IO] {
      override def list(offset: Int, limit: Int): IO[List[Url]] =
        IO(data.drop(offset).take(limit))
      override def get(code: Code): IO[Option[Url]] =
        IO(data.filter(_.code === code).headOption)
      override def create(url: Url): IO[Code] =
        IO(url.code)
      override def hit(code: Code): IO[Unit] =
        IO.unit
    }

  private def mkSlientShortener: Shortener[IO] =
    addr => Url(Code("slient"), addr, Hit(0), Some(utcNow)).pure[IO]

  private def mkFailingShortener: Shortener[IO] =
    addr => IO.raiseError(Shortener.NotStoredError)

  test("list-urls:first-page") {
    forall(Gen.listOf(urlsGen)) { l =>
      val urls      = mkUrls(l)
      val shortener = mkSlientShortener
      val routes    = UrlRoutes[IO](urls, shortener).routes
      val req       = GET(uri"/urls")

      routes.run(req).value.flatMap {
        case None => failure("endpoint not found").pure[IO]
        case Some(res) =>
          res.asJson.map { json =>
            expect(res.status == Status.Ok) &&
            expect(json.dropNullValues == l.take(10).asJson.dropNullValues)
          }
      }
    }
  }

  test("list-urls:second-page-with-limit:20") {
    forall(Gen.listOf(urlsGen)) { l =>
      val urls      = mkUrls(l)
      val shortener = mkSlientShortener
      val routes    = UrlRoutes[IO](urls, shortener).routes
      val req       = GET(uri"/urls?offset=10&limit=20")

      routes.run(req).value.flatMap {
        case None => failure("endpoint not found").pure[IO]
        case Some(res) =>
          res.asJson.map { json =>
            expect(res.status == Status.Ok) &&
            expect(json.dropNullValues == l.drop(10).take(20).asJson.dropNullValues)
          }
      }
    }
  }

  test("get-urls") {
    forall(urlsGen) { u =>
      val urls      = mkUrls(List(u))
      val shortener = mkSlientShortener
      val routes    = UrlRoutes[IO](urls, shortener).routes
      val req       = GET(Uri.unsafeFromString(s"/urls/${u.code.value}"))

      routes.run(req).value.flatMap {
        case None => failure("endpoint not found").pure[IO]
        case Some(res) =>
          res.asJson.map { json =>
            expect(res.status == Status.Ok) &&
            expect(json.dropNullValues == u.asJson.dropNullValues)
          }
      }
    }
  }

  test("get-urls:not-found") {
    val urls      = mkUrls(Nil)
    val shortener = mkSlientShortener
    val routes    = UrlRoutes[IO](urls, shortener).routes
    val req       = GET(uri"/urls/non-existing")

    routes.run(req).value.flatMap {
      case None => failure("endpoint not found").pure[IO]
      case Some(res) =>
        expect(res.status == Status.NotFound).pure[IO]
    }
  }

  test("create:invalid-addr") {
    val urls      = mkUrls(Nil)
    val shortener = mkSlientShortener
    val routes    = UrlRoutes[IO](urls, shortener).routes
    val req       = POST(json"""{"url": "xxx"}""", uri"/urls")

    routes.run(req).value.flatMap {
      case None      => failure("endpoint not found").pure[IO]
      case Some(res) => expect(res.status == Status.UnprocessableEntity).pure[IO]
    }
  }

  test("create") {
    val urls      = mkUrls(Nil)
    val shortener = mkSlientShortener
    val routes    = UrlRoutes[IO](urls, shortener).routes
    val req       = POST(json"""{"url": "https://test-success.local"}""", uri"/urls")

    routes.run(req).value.flatMap {
      case None => failure("endpoint not found").pure[IO]
      case Some(res) =>
        res.asJson.map { json =>
          val c = json.hcursor
          expect(res.status == Status.Created) &&
          expect(c.get[String]("code").toOption === Some("slient")) &&
          expect(c.get[String]("address").toOption === Some("https://test-success.local")) &&
          expect(c.get[Int]("hit").toOption === Some(0)) &&
          expect(c.get[String]("created_at").toOption.nonEmpty)
        }
    }
  }

  test("create:failure") {
    val urls      = mkUrls(Nil)
    val shortener = mkFailingShortener
    val routes    = UrlRoutes[IO](urls, shortener).routes
    val req       = POST(json"""{"url": "https://test-success.local"}""", uri"/urls")

    routes.run(req).value.flatMap {
      case None => failure("endpoint not found").pure[IO]
      case Some(res) =>
        expect(res.status === Status.InternalServerError).pure[IO]
    }
  }
}

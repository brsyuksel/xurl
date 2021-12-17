package xurl.services

import xurl.url.Urls
import xurl.url.model._

import cats.effect._
import cats.implicits._
import weaver.SimpleIOSuite

object ShortenerSuite extends SimpleIOSuite {
  test("shorten") {
    val shortener = Shortener.make(mkSerialCode("succ"), mkDummyUrls)
    shortener
      .shorten(Address("dummy-addr"))
      .map(u => expect(u.code.value === "succ"))
  }

  test("shorten:error") {
    val shortener = Shortener.make(mkSerialCode("error"), mkDummyUrls)
    shortener
      .shorten(Address("dummy-addr"))
      .attempt
      .map(e => expect(e.isLeft) && expect(e == Left(Shortener.NotStoredError)))
  }

  private def mkSerialCode(code: String): SerialCode[IO] =
    new SerialCode[IO] {
      def next: IO[String] = IO(code)
    }

  private lazy val mkDummyUrls: Urls[IO] =
    new Urls[IO] {
      override def list(limit: Int, offset: Int): IO[List[Url]] =
        IO(List.empty[Url])
      override def get(code: Code): IO[Option[Url]] =
        (code.value match {
          case "succ" => Url(code, Address("dummy-addr")).some
          case _      => none[Url]
        }).pure[IO]
      override def create(url: Url): IO[Code] =
        IO(url.code)
      override def hit(code: Code): IO[Unit] =
        IO.unit
    }
}

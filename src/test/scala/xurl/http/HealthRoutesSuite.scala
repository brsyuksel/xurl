package xurl.http

import xurl.health.model.{ Status => HStat, _ }
import xurl.services.HealthCheck

import _root_.play.api.libs.json._
import cats.effect._
import cats.implicits._
import fs2.Chunk
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.syntax.literals._
import org.scalacheck.Gen
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object HealthRoutesSuite extends SimpleIOSuite with Checkers {

  // EntityEncoder/EntityDecoder for play-json types
  implicit def jsValueEntityEncoder: EntityEncoder[IO, JsValue] =
    EntityEncoder[IO, Chunk[Byte]]
      .contramap[JsValue] { json =>
        val bytes = Json.stringify(json).getBytes(java.nio.charset.StandardCharsets.UTF_8)
        Chunk.array(bytes)
      }
      .withContentType(org.http4s.headers.`Content-Type`(MediaType.application.json))

  implicit def jsValueEntityDecoder: EntityDecoder[IO, JsValue] =
    EntityDecoder.text[IO].map(Json.parse)

  private def mkHealthCheck(s: Boolean, c: Boolean): HealthCheck[IO] =
    new HealthCheck[IO] {
      def status: IO[HStat] =
        HStat(Storage(s), Cache(c)).pure[IO]
    }

  val tupleBoolGen: Gen[(Boolean, Boolean)] =
    for {
      s <- Gen.oneOf(true, false)
      c <- Gen.oneOf(true, false)
    } yield (s, c)

  test("health status") {
    forall(tupleBoolGen) { case (s, c) =>
      val health   = mkHealthCheck(s, c)
      val routes   = HealthRoutes[IO](health).routes
      val req      = GET(uri"/")
      val expected = Json.obj("storage" -> JsBoolean(s), "cache" -> JsBoolean(c))

      routes.run(req).value.flatMap {
        case None => failure("endpoint not found").pure[IO]
        case Some(res) =>
          res.as[JsValue].map { json =>
            expect(json == expected)
          }
      }
    }
  }
}

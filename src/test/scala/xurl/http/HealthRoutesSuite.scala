package xurl.http

import xurl.health.model.{ Status => HStat, _ }
import xurl.services.HealthCheck

import cats.effect._
import cats.implicits._
import io.circe.literal._
import io.circe.syntax._
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.dsl.io._
import org.http4s.syntax.literals._
import org.scalacheck.Gen
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object HealthRoutesSuite extends SimpleIOSuite with Checkers {
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
      val expected = json"""{"storage": $s, "cache": $c}"""

      routes.run(req).value.flatMap {
        case None => failure("endpoint not found").pure[IO]
        case Some(res) =>
          res.asJson.map { json =>
            expect(json.dropNullValues == expected.asJson.dropNullValues)
          }
      }
    }
  }
}

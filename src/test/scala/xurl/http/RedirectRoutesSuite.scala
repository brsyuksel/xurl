package xurl.http

import xurl.services.Routing
import xurl.url.model.Address

import cats.effect._
import cats.implicits._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.syntax.literals._
import weaver.SimpleIOSuite

object RedirectRoutesSuite extends SimpleIOSuite {
  private def mkRouting: Routing[IO] =
    code =>
      code.value match {
        case "succ" => IO(Address("https://testing-xurl.local"))
        case _      => IO.raiseError(Routing.RouteNotFoundError)
      }

  test("redirect:not-found") {
    val routing = mkRouting
    val routes  = RedirectRoutes[IO](routing).routes
    val req     = GET(uri"/fail")

    routes.run(req).value.flatMap {
      case None => failure("endpoint not found").pure[IO]
      case Some(res) =>
        expect(res.status === Status.NotFound).pure[IO]
    }
  }

  test("redirect") {
    val routing = mkRouting
    val routes  = RedirectRoutes[IO](routing).routes
    val req     = GET(uri"/succ")

    routes.run(req).value.flatMap {
      case None => failure("endpoint not found").pure[IO]
      case Some(res) =>
        val location = res.headers.headers.filter(_.name.toString == "Location").map(_.value).headOption
        val t = expect(res.status === Status.MovedPermanently) &&
          expect(location === Some("https://testing-xurl.local"))

        t.pure[IO]
    }
  }
}

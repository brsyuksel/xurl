package xurl.health

import cats.effect._
import natchez.Trace.Implicits.noop
import skunk._
import weaver.IOSuite

object SkunkHealthSuite extends IOSuite {
  type Res = Resource[IO, Session[IO]]

  override def sharedResource: Resource[IO, Res] =
    Session.pooled[IO](
      host = "localhost",
      port = 5432,
      user = "postgres",
      database = "xurl",
      password = Some("postgres"),
      max = 10
    )

  test("ok returns true") { pg =>
    val health = new SkunkHealth[IO](pg)
    health.ok.map(expect(_))
  }
}

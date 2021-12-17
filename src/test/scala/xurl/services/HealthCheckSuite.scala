package xurl.services

import xurl.health.Health

import cats.effect._
import cats.implicits._
import org.scalacheck.Gen
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object HealthCheckSuite extends SimpleIOSuite with Checkers {
  private def dummyHealth(b: Boolean): Health[IO] =
    new Health[IO] {
      def ok: IO[Boolean] = b.pure[IO]
    }

  val tupleBoolGen: Gen[(Boolean, Boolean)] =
    for {
      s <- Gen.oneOf(true, false)
      c <- Gen.oneOf(true, false)
    } yield (s, c)

  test("status") {
    forall(tupleBoolGen) { case (s, c) =>
      val dummyStorage = dummyHealth(s)
      val dummyCache   = dummyHealth(c)
      val h            = HealthCheck.make(dummyStorage, dummyCache)
      h.status.map { status =>
        expect(status.storage.value === s) &&
        expect(status.cache.value === c)
      }
    }
  }
}

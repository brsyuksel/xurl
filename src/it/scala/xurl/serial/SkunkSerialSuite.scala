package xurl.serial

import cats.effect._
import cats.implicits._
import skunk._
import skunk.implicits._
import natchez.Trace.Implicits.noop
import weaver.IOSuite

object SkunkSerialSuite extends IOSuite {
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
  // skunk throws (and ignores) exception for query below because it doesn't have capability to parse it
  // .evalTap(_.use(_.execute(sql"ALTER SEQUENCE serial_int RESTART".command)))

  test("positive number") { pg =>
    val s = new SkunkSerial[IO](pg)
    s.get.map(l => expect(l > 0))
  }

  test("monotonically increasing") { pg =>
    val s = new SkunkSerial[IO](pg)
    List.fill(10)(s.get).sequence.map { l =>
      val res =
        l
          .sliding(2)
          .map(n => n.head < n.last)
          .foldLeft(true)(_ && _)
      expect(res)
    }
  }
}

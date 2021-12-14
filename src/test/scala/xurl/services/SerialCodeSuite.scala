package xurl.services

import cats.effect._
import cats.implicits._
import weaver.SimpleIOSuite

import xurl.serial.{ BaseNPositive, Serial }

object SerialCodeSuite extends SimpleIOSuite {
  test("next uniqueness") {
    makeIncrementingSerial >>= { serial =>
      val serialCode = SerialCode.make[IO](serial, makeBase15)
      List
        .fill(100)(serialCode.next)
        .sequence
        .map(_.toSet.size)
        .map(s => expect(s == 100))
    }
  }

  private def makeIncrementingSerial: IO[Serial[IO]] =
    Ref.of[IO, Long](0).map { r =>
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
}

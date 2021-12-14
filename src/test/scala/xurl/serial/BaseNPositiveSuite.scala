package xurl.serial

import cats.effect.IO

import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers
import org.scalacheck.Gen

object BaseNPositiveSuite extends SimpleIOSuite with Checkers {
  private val letters = "abc123def456"
  private val base12  = new BaseNPositive[IO](letters)

  test("NumberIsNotPositive error") {
    base12.encode(-1).attempt.map { e =>
      expect(e.isLeft) &&
      expect(e == Left(BaseNPositive.NumberIsNotPositive))
    }
  }

  test("encoding equality") {
    def decode(l: String, c: String): Long =
      c.zip(c.indices.reverse)
        .map { case (c, i) =>
          l.indexOf(c) * scala.math.pow(l.length, i).toLong
        }
        .sum

    forall(Gen.posNum[Long]) { n =>
      base12.encode(n).map(s => expect(decode(letters, s) == n))
    }
  }
}

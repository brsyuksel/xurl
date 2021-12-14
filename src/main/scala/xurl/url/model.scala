package xurl.url

import java.time.LocalDateTime

import cats.Show
import io.estatico.newtype.macros.newtype
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.{ Url => RUrl }
import eu.timepit.refined.collection.NonEmpty
import derevo.cats.{ eqv, show }
import derevo.derive
import derevo.circe.magnolia.{ customizableEncoder, encoder }
import io.circe.magnolia.configured.{ Configuration => JsonConf }

object model {
  implicit val localDateTimeShow: Show[LocalDateTime] =
    Show.show[LocalDateTime](_.toString)

  @derive(show, eqv, encoder)
  @newtype
  case class Code(value: String)

  @derive(show, eqv, encoder)
  @newtype
  case class Address(value: String)

  @derive(show, encoder)
  @newtype
  case class Hit(value: Long)

  @derive(show, customizableEncoder)
  case class Url(
      code: Code,
      address: Address,
      hit: Hit = Hit(0),
      createdAt: Option[LocalDateTime] = None
  )
  object Url {
    implicit val c: JsonConf = JsonConf.default.withSnakeCaseMemberNames
  }
}

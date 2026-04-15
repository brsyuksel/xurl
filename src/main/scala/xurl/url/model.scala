package xurl.url

import java.time.LocalDateTime

import cats.Show
import derevo.cats.{ eqv, show }
import derevo.derive
import io.estatico.newtype.macros.newtype
import play.api.libs.json._

object model {
  implicit val localDateTimeShow: Show[LocalDateTime] =
    Show.show[LocalDateTime](_.toString)

  @derive(show, eqv)
  @newtype
  case class Code(value: String)
  object Code {
    implicit val format: Format[Code] = new Format[Code] {
      override def reads(json: JsValue): JsResult[Code] = json match {
        case JsString(s) => JsSuccess(Code(s))
        case _           => JsError("Expected string for Code")
      }
      override def writes(o: Code): JsValue = JsString(o.value)
    }
  }

  @derive(show, eqv)
  @newtype
  case class Address(value: String)
  object Address {
    implicit val format: Format[Address] = new Format[Address] {
      override def reads(json: JsValue): JsResult[Address] = json match {
        case JsString(s) => JsSuccess(Address(s))
        case _           => JsError("Expected string for Address")
      }
      override def writes(o: Address): JsValue = JsString(o.value)
    }
  }

  @derive(show)
  @newtype
  case class Hit(value: Long)
  object Hit {
    implicit val format: Format[Hit] = new Format[Hit] {
      override def reads(json: JsValue): JsResult[Hit] = json match {
        case JsNumber(n) => JsSuccess(Hit(n.toLong))
        case _           => JsError("Expected number for Hit")
      }
      override def writes(o: Hit): JsValue = JsNumber(o.value)
    }
  }

  @derive(show)
  case class Url(
      code: Code,
      address: Address,
      hit: Hit = Hit(0),
      createdAt: Option[LocalDateTime] = None
  )
  object Url {
    implicit val localDateTimeFormat: Format[LocalDateTime] = new Format[LocalDateTime] {
      override def reads(json: JsValue): JsResult[LocalDateTime] = json match {
        case JsString(s) =>
          try {
            JsSuccess(LocalDateTime.parse(s))
          } catch {
            case _: Exception => JsError(s"Invalid LocalDateTime format: $s")
          }
        case _ => JsError("Expected string for LocalDateTime")
      }

      override def writes(o: LocalDateTime): JsValue = JsString(o.toString)
    }

    implicit val format: Format[Url] = new Format[Url] {
      override def reads(json: JsValue): JsResult[Url] = {
        for {
          code      <- (json \ "code").validate[Code]
          address   <- (json \ "address").validate[Address]
          hit       <- (json \ "hit").validateOpt[Hit].map(_.getOrElse(Hit(0)))
          createdAt <- (json \ "created_at").validateOpt[LocalDateTime]
        } yield Url(code, address, hit, createdAt)
      }

      override def writes(o: Url): JsValue = {
        val base = Json.obj(
          "code"    -> Json.toJson(o.code),
          "address" -> Json.toJson(o.address),
          "hit"     -> Json.toJson(o.hit)
        )
        o.createdAt.fold(base)(dt => base + ("created_at" -> Json.toJson(dt)))
      }
    }
  }
}

package xurl.health

import derevo.cats.{ eqv, show }
import derevo.derive
import io.estatico.newtype.macros.newtype
import play.api.libs.json._

object model {
  @derive(eqv, show)
  @newtype
  case class Storage(value: Boolean)
  object Storage {
    implicit val format: Format[Storage] = new Format[Storage] {
      override def reads(json: JsValue): JsResult[Storage] = json match {
        case JsBoolean(b) => JsSuccess(Storage(b))
        case _            => JsError("Expected boolean for Storage")
      }
      override def writes(o: Storage): JsValue = JsBoolean(o.value)
    }
  }

  @derive(eqv, show)
  @newtype
  case class Cache(value: Boolean)
  object Cache {
    implicit val format: Format[Cache] = new Format[Cache] {
      override def reads(json: JsValue): JsResult[Cache] = json match {
        case JsBoolean(b) => JsSuccess(Cache(b))
        case _            => JsError("Expected boolean for Cache")
      }
      override def writes(o: Cache): JsValue = JsBoolean(o.value)
    }
  }

  @derive(eqv, show)
  case class Status(storage: Storage, cache: Cache)
  object Status {
    implicit val format: Format[Status] = Json.format[Status]
  }
}

package xurl.http

import xurl.url.model.Address

import eu.timepit.refined.api.{ Refined, Validate }
import eu.timepit.refined.string.Url
import eu.timepit.refined.refineV
import io.estatico.newtype.macros.newtype
import play.api.libs.json._

object params {
  // Custom Reads/Writes for refined types
  implicit def refinedReads[T, P](implicit reads: Reads[T], validate: Validate[T, P]): Reads[T Refined P] =
    Reads[T Refined P] { json =>
      reads.reads(json).flatMap { value =>
        refineV[P](value) match {
          case Right(refined) => JsSuccess(refined)
          case Left(error)    => JsError(s"Refinement failed: $error")
        }
      }
    }

  implicit def refinedWrites[T, P](implicit writes: Writes[T]): Writes[T Refined P] =
    Writes[T Refined P] { refined =>
      writes.writes(refined.value)
    }

  @newtype
  case class AddressParam(value: String Refined Url) {
    def toAddress: Address = Address(value.value)
  }
  object AddressParam {
    implicit val reads: Reads[AddressParam] = (json: JsValue) => {
      (json \ "url").validate[String Refined Url].map(AddressParam.apply)
    }
  }
}

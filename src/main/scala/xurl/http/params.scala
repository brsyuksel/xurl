package xurl.http

import xurl.url.model.Address

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import io.circe.Decoder
import io.circe.refined._
import io.estatico.newtype.macros.newtype

object params {
  @newtype
  case class AddressParam(value: String Refined Url) {
    def toAddress: Address = Address(value.value)
  }
  object AddressParam {
    implicit val dec: Decoder[AddressParam] =
      Decoder.forProduct1("url")(AddressParam.apply)
  }
}

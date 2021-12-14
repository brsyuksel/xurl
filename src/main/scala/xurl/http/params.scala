package xurl.http

import io.estatico.newtype.macros.newtype
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import io.circe.Decoder
import io.circe.refined._

import xurl.url.model.Address

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

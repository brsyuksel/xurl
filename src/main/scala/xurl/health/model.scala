package xurl.health

import derevo.circe.magnolia.encoder
import derevo.derive
import io.estatico.newtype.macros.newtype

object model {
  @derive(encoder)
  @newtype
  case class Storage(value: Boolean)

  @derive(encoder)
  @newtype
  case class Cache(value: Boolean)

  @derive(encoder)
  case class Status(storage: Storage, cache: Cache)
}

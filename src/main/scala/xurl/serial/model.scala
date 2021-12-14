package xurl.serial

import scala.util.control.NoStackTrace

object model {
  case class SerialError(message: String) extends NoStackTrace
}

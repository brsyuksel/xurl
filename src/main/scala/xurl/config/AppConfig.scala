package xurl.config

import scala.concurrent.duration.FiniteDuration
import scala.util.control.NoStackTrace

import cats._
import cats.implicits._
import cats.effect._
import pureconfig._
import pureconfig.generic.auto._

case class Server(host: String, port: Int)
case class DB(host: String, port: Int, user: String, password: Option[String], database: String, connections: Int)
case class Cache(url: String, expiration: FiniteDuration)
case class BaseN(letters: String)

case class AppConfig(server: Server, db: DB, cache: Cache, basen: BaseN)
object AppConfig {
  case class ConfigurationError(message: String) extends NoStackTrace

  private def loadConf =
    ConfigSource.default.load[AppConfig].leftMap(e => ConfigurationError(e.toString))

  def load[F[_]: ApplicativeThrow]: F[AppConfig] =
    ApplicativeThrow[F].fromEither(loadConf)
}

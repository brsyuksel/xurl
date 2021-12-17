package xurl

import xurl.config.AppConfig
import xurl.modules._
import xurl.resources._

import cats.effect._
import dev.profunktor.redis4cats.log4cats._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object main extends IOApp.Simple {
  implicit val logger = Slf4jLogger.getLogger[IO]
  override def run: IO[Unit] =
    AppConfig.load[IO].flatMap { conf =>
      Logger[IO].info(s"loaded configuration: $conf") *>
        AppResources.make[IO](conf).use { resources =>
          val services   = Services.make[IO](resources.pg, resources.redis, conf.cache.expiration, conf.basen.letters)
          val httpAPI    = HttpAPI.make[IO](services)
          val httpServer = HttpServer.make[IO](conf, httpAPI.httpApp)
          httpServer.server.useForever
        }
    }
}

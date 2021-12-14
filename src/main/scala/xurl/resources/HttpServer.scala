package xurl.resources

import cats.effect._
import org.http4s.HttpApp
import org.http4s.server.Server
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s.{ Host, Port }

import xurl.config.AppConfig

trait HttpServer[F[_]] {
  def server: Resource[F, Server]
}

object HttpServer {
  def make[F[_]: Async](conf: AppConfig, httpApp: HttpApp[F]): HttpServer[F] =
    new HttpServer[F] {
      def server: Resource[F, Server] = {
        EmberServerBuilder
          .default[F]
          .withHostOption(Host.fromString(conf.server.host))
          .withPort(Port.fromInt(conf.server.port).get)
          .withHttpApp(httpApp)
          .build
      }
    }
}

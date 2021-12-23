package xurl.resources

import xurl.config.AppConfig

import cats.effect._
import com.comcast.ip4s.{ Host, Port }
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server

trait HttpServer[F[_]] {
  def server: Resource[F, Server]
}

object HttpServer {
  def make[F[_]: Async](conf: AppConfig, httpAppR: Resource[F, HttpApp[F]]): HttpServer[F] =
    new HttpServer[F] {
      def server: Resource[F, Server] = httpAppR.flatMap { httpApp =>
        EmberServerBuilder
          .default[F]
          .withHostOption(Host.fromString(conf.server.host))
          .withPort(Port.fromInt(conf.server.port).get)
          .withHttpApp(httpApp)
          .build
      }
    }
}

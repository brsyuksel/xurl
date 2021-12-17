package xurl.http

import xurl.services.HealthCheck

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

final case class HealthRoutes[F[_]: Concurrent](healthCheck: HealthCheck[F]) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    healthCheck.status
      .flatMap(Ok(_))
  }
}

package xurl.http

import xurl.services.HealthCheck

import _root_.play.api.libs.json._
import cats.effect._
import cats.implicits._
import fs2.Chunk
import org.http4s._
import org.http4s.dsl.Http4sDsl

final case class HealthRoutes[F[_]: Concurrent](healthCheck: HealthCheck[F]) extends Http4sDsl[F] {

  // Manual EntityEncoder instance for play-json
  implicit def playJsonEncoder[A](implicit writes: Writes[A]): EntityEncoder[F, A] =
    EntityEncoder[F, Chunk[Byte]]
      .contramap[A] { a =>
        val json  = Json.toJson(a)
        val bytes = Json.stringify(json).getBytes(java.nio.charset.StandardCharsets.UTF_8)
        Chunk.array(bytes)
      }
      .withContentType(org.http4s.headers.`Content-Type`(MediaType.application.json))

  val routes: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    healthCheck.status
      .flatMap(Ok(_))
  }
}

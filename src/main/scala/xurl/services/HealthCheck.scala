package xurl.services

import xurl.health.Health
import xurl.health.model._

import cats.effect._
import cats.effect.implicits._
import cats.syntax.all._

trait HealthCheck[F[_]] {
  def status: F[Status]
}

object HealthCheck {
  def make[F[_]: Temporal](storage: Health[F], cache: Health[F]): HealthCheck[F] =
    new HealthCheck[F] {
      private def storageOk: F[Storage] =
        storage.ok.map(Storage(_))

      private def cacheOk: F[Cache] =
        cache.ok.map(Cache(_))

      def status: F[Status] =
        (storageOk, cacheOk).parMapN(Status.apply)
    }
}

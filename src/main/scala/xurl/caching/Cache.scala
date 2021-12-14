package xurl.caching

trait Cache[F[_]] {
  def through(key: String, fk: => F[Option[String]]): F[Option[String]]
}

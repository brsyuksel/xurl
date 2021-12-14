package xurl.serial

trait Serial[F[_]] {
  def get: F[Long]
}

trait BaseN[F[_]] {
  def encode(n: Long): F[String]
}

package xurl.health

trait Health[F[_]] {
  def ok: F[Boolean]
}

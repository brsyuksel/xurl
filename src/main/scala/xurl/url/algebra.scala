package xurl.url

import model._

trait Urls[F[_]] {
  def list(offset: Int, limit: Int): F[List[Url]]
  def get(code: Code): F[Option[Url]]
  def create(url: Url): F[Code]
  def hit(code: Code): F[Unit]
}

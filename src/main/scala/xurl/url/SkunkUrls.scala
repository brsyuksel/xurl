package xurl.url

import xurl.url.model._

import cats.effect._
import cats.implicits._
import skunk._
import skunk.codec.all._
import skunk.implicits._

final case class SkunkUrls[F[_]: Concurrent](pg: Resource[F, Session[F]]) extends Urls[F] {
  import SkunkUrls._

  override def list(offset: Int, limit: Int): F[List[Url]] =
    pg.use { s =>
      s.prepare(SQL.paginatedList).use { p =>
        p.stream(limit ~ offset, 1024).compile.toList
      }
    }

  override def get(code: Code): F[Option[Url]] =
    pg.use { s =>
      s.prepare(SQL.getByCode).use { p =>
        p.option(code)
      }
    }

  // TODO: handle code uniqueness error
  override def create(url: Url): F[Code] =
    pg.use { s =>
      s.prepare(SQL.insertUrl).use { p =>
        p.execute(url.code ~ url.address).as(url.code)
      }
    }

  override def hit(code: Code): F[Unit] =
    pg.use { s =>
      s.prepare(SQL.increaseHit).use { p =>
        p.execute(code).void
      }
    }

}

object SkunkUrls {
  private case object SQL {
    val paginatedList: Query[Int ~ Int, Url] =
      sql"""
      SELECT *
        FROM urls
       LIMIT $int4
      OFFSET $int4
      """.query(url)

    val getByCode: Query[Code, Url] =
      sql"""
      SELECT *
        FROM urls
       WHERE code = $code
      """.query(url)

    val insertUrl: Command[Code ~ Address] =
      sql"""
      INSERT INTO urls (code, address)
      VALUES ($code, $address)
      """.command

    val increaseHit: Command[Code] =
      sql"""
      UPDATE urls
         SET hit = hit + 1
       WHERE code = $code
      """.command
  }

  val code: Codec[Code]       = varchar.imap[Code](Code(_))(_.value)
  val address: Codec[Address] = varchar.imap[Address](Address(_))(_.value)
  val hit: Codec[Hit]         = int8.imap[Hit](Hit(_))(_.value)
  val url: Codec[Url] =
    (code ~ address ~ hit ~ timestamp.opt).imap { case c ~ a ~ h ~ t =>
      Url(c, a, h, t)
    }(u => u.code ~ u.address ~ u.hit ~ u.createdAt)
}

import sbt._

object Dependencies {
  object versions {
    val catsEffect       = "3.5.1"
    val cats             = "2.10.0"
    val newtype          = "0.4.4"
    val refined          = "0.11.0"
    val derevo           = "0.13.0"
    val log4cats         = "2.6.0"
    val redis            = "1.4.3"
    val skunk            = "0.5.1"
    val circe            = "0.14.6"
    val http4s           = "0.23.23"
    val http4sPrometheus = "0.24.4"
    val slf4j            = "2.0.9"
    val pureConfig       = "0.17.4"

    val weaver = "0.8.3"

    val scalafixOrganizeImports = "0.6.0"
  }

  object libraries {
    val cats                = "org.typelevel"         %% "cats-core"                 % versions.cats
    val catsEffect          = "org.typelevel"         %% "cats-effect"               % versions.catsEffect
    val newtype             = "io.estatico"           %% "newtype"                   % versions.newtype
    val refined             = "eu.timepit"            %% "refined"                   % versions.refined
    val derevoCore          = "tf.tofu"               %% "derevo-core"               % versions.derevo
    val derevoCats          = "tf.tofu"               %% "derevo-cats"               % versions.derevo
    val log4cats            = "org.typelevel"         %% "log4cats-slf4j"            % versions.log4cats
    val slf4jSimple         = "org.slf4j"              % "slf4j-simple"              % versions.slf4j
    val skunk               = "org.tpolecat"          %% "skunk-core"                % versions.skunk
    val redis               = "dev.profunktor"        %% "redis4cats-effects"        % versions.redis
    val redisL4C            = "dev.profunktor"        %% "redis4cats-log4cats"       % versions.redis
    val circeCore           = "io.circe"              %% "circe-core"                % versions.circe
    val circeGeneric        = "io.circe"              %% "circe-generic"             % versions.circe
    val circeParser         = "io.circe"              %% "circe-parser"              % versions.circe
    val circeRefined        = "io.circe"              %% "circe-refined"             % versions.circe
    val derevoCirceMagnolia = "tf.tofu"               %% "derevo-circe-magnolia"     % versions.derevo
    val http4sDsl           = "org.http4s"            %% "http4s-dsl"                % versions.http4s
    val http4sEmberServer   = "org.http4s"            %% "http4s-ember-server"       % versions.http4s
    val http4sEmberClient   = "org.http4s"            %% "http4s-ember-client"       % versions.http4s
    val http4sCirce         = "org.http4s"            %% "http4s-circe"              % versions.http4s
    val http4sPrometheus    = "org.http4s"            %% "http4s-prometheus-metrics" % versions.http4sPrometheus
    val pureConfig          = "com.github.pureconfig" %% "pureconfig"                % versions.pureConfig

    val weaverCats       = "com.disneystreaming" %% "weaver-cats"       % versions.weaver
    val weaverDiscipline = "com.disneystreaming" %% "weaver-discipline" % versions.weaver
    val weaverScalaCheck = "com.disneystreaming" %% "weaver-scalacheck" % versions.weaver
    val log4catsNoOp     = "org.typelevel"       %% "log4cats-noop"     % versions.log4cats
    val circeLiteral     = "io.circe"            %% "circe-literal"     % versions.circe

    val scalafixOrganizeImports = "com.github.liancheng" %% "organize-imports" % versions.scalafixOrganizeImports
  }

  val runtime = Seq(
    libraries.cats,
    libraries.catsEffect,
    libraries.newtype,
    libraries.refined,
    libraries.derevoCore,
    libraries.derevoCats,
    libraries.log4cats,
    libraries.slf4jSimple,
    libraries.skunk,
    libraries.redis,
    libraries.redisL4C,
    libraries.circeCore,
    libraries.circeGeneric,
    libraries.circeParser,
    libraries.circeRefined,
    libraries.derevoCirceMagnolia,
    libraries.http4sDsl,
    libraries.http4sEmberServer,
    libraries.http4sEmberClient,
    libraries.http4sCirce,
    libraries.http4sPrometheus,
    libraries.pureConfig
  )

  val testing = Seq(
    libraries.weaverCats,
    libraries.weaverDiscipline,
    libraries.weaverScalaCheck,
    libraries.log4catsNoOp,
    libraries.circeLiteral
  ).map(_ % "it,test")

  val overrides = Seq(
    libraries.circeCore,
  )

  val dependencies = runtime ++ testing
}

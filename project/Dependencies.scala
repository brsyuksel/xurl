import sbt._

object Dependencies {
  object versions {
    val catsEffect       = "3.7.0"
    val cats             = "2.13.0"
    val newtype          = "0.4.4"
    val refined          = "0.11.3"
    val derevo           = "0.14.0"
    val log4cats         = "2.8.0"
    val skunk            = "0.6.5"
    val playJson         = "3.0.4"
    val redis            = "2.0.3"
    val http4sPrometheus = "0.25.0"
    val http4s           = "0.23.34"
    val slf4j            = "2.0.17"
    val pureConfig       = "0.17.10"

    val weaver = "0.12.0"

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
    val playJson            = "org.playframework"     %% "play-json"                 % versions.playJson
    val playJsonDerived     = "org.julienrf"          %% "play-json-derived-codecs"  % "11.0.0"
    val http4sDsl            = "org.http4s"            %% "http4s-dsl"                % versions.http4s
    val http4sEmberServer    = "org.http4s"            %% "http4s-ember-server"       % versions.http4s
    val http4sEmberClient    = "org.http4s"            %% "http4s-ember-client"       % versions.http4s
    val http4sPlayJson       = "org.http4s"            %% "http4s-play-json"          % "0.23.15"
    val http4sPrometheus     = "org.http4s"            %% "http4s-prometheus-metrics" % versions.http4sPrometheus
    val pureConfig           = "com.github.pureconfig" %% "pureconfig"                % versions.pureConfig

    val weaverCats       = "org.typelevel" %% "weaver-cats"       % versions.weaver
    val weaverDiscipline = "org.typelevel" %% "weaver-discipline" % versions.weaver
    val weaverScalaCheck = "org.typelevel" %% "weaver-scalacheck" % versions.weaver
    val log4catsNoOp     = "org.typelevel" %% "log4cats-noop"     % versions.log4cats

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
    libraries.playJson,
    libraries.playJsonDerived,
    libraries.http4sDsl,
    libraries.http4sEmberServer,
    libraries.http4sEmberClient,
    libraries.http4sPlayJson,
    libraries.http4sPrometheus,
    libraries.pureConfig
  )

  val testing = Seq(
    libraries.weaverCats,
    libraries.weaverDiscipline,
    libraries.weaverScalaCheck,
    libraries.log4catsNoOp
  ).map(_ % "it,test")

  val overrides = Seq(
    libraries.playJson
  )

  val dependencies = runtime ++ testing
}

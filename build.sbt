import Dependencies._

ThisBuild / scalaVersion     := "2.13.7"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "xurl"
ThisBuild / organizationName := "xurl"
ThisBuild / scalafixDependencies += libraries.scalafixOrganizeImports
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

val scalafixSettings = inConfig(IntegrationTest)(scalafixConfigSettings(IntegrationTest))
lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    name := "xurl",
    scalacOptions ++= List("-Ymacro-annotations", "-Yrangepos", "-Wconf:cat=unused:info", "-Ywarn-unused"),
    Defaults.itSettings,
    scalafixSettings,
    testFrameworks += TestFramework("weaver.framework.CatsEffect"),
    libraryDependencies ++= dependencies,
    packGenerateWindowsBatFile := false,
    packMain                   := Map("xurl" -> "xurl.main")
  )
  .enablePlugins(PackPlugin)

addCommandAlias("sfix", ";scalafixAll --rules OrganizeImports")
addCommandAlias("scalafixCheck", ";scalafixAll --check --rules OrganizeImports")
addCommandAlias("fmt", ";scalafmtAll")
addCommandAlias("runTests", ";test;it:test")
addCommandAlias("ci", ";scalafixCheck;scalafmtCheckAll;runTests")

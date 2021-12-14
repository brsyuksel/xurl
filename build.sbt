import Dependencies._

ThisBuild / scalaVersion     := "2.13.7"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "xurl"
ThisBuild / organizationName := "xurl"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    name := "xurl",
    scalacOptions ++= List("-Ymacro-annotations", "-Yrangepos", "-Wconf:cat=unused:info"),
    Defaults.itSettings,
    testFrameworks += TestFramework("weaver.framework.CatsEffect"),
    libraryDependencies ++= dependencies
  )

addCommandAlias("fmt", "scalafmtAll")
addCommandAlias("ci", "scalafmtCheckAll;test;it:test")

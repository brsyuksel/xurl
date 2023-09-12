import Dependencies._

ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "xurl"
ThisBuild / organizationName := "xurl"
ThisBuild / scalafixDependencies += libraries.scalafixOrganizeImports
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / assemblyMergeStrategy := {
  case x if x.contains("io.netty.versions.properties") => MergeStrategy.concat
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

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
    assembly / mainClass       := Some("xurl.main"),
    assembly / assemblyJarName := "xurl-assembly.jar",
    // java -agentlib:native-image-agent=config-output-dir=./ci/native-image-configs -jar xurl-assembly.jar
    graalVMNativeImageOptions ++= Seq(
      "--allow-incomplete-classpath",
      "--enable-http",
      "--enable-https",
      "-H:+AllowVMInspection",
      "-H:ResourceConfigurationFiles=../../ci/native-image-configs/resource-config.json",
      "-H:ReflectionConfigurationFiles=../../ci/native-image-configs/reflect-config.json",
      "-H:JNIConfigurationFiles=../../ci/native-image-configs/jni-config.json",
      "-H:DynamicProxyConfigurationFiles=../../ci/native-image-configs/proxy-config.json"
    )
  )
  .enablePlugins(GraalVMNativeImagePlugin)

addCommandAlias("sfix", ";scalafixAll --rules OrganizeImports")
addCommandAlias("scalafixCheck", ";scalafixAll --check --rules OrganizeImports")
addCommandAlias("fmt", ";scalafmtAll")
addCommandAlias("runTests", ";test;it:test")
addCommandAlias("ci", ";scalafixCheck;scalafmtCheckAll;runTests")

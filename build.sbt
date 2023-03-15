ThisBuild / organization := "squareoneinsights"
ThisBuild / version := "1.0"

// the Scala version that will be used for cross-compiled libraries
ThisBuild / scalaVersion := "2.13.8"

// Workaround for scala-java8-compat issue affecting Lagom dev-mode
// https://github.com/lagom/lagom/issues/3344
ThisBuild / libraryDependencySchemes +=
  "org.scala-lang.modules" %% "scala-java8-compat" % VersionScheme.Always

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test

lazy val `employee-api` = (project in file("."))
  .aggregate(`employee-api-api`, `employee-api-impl`, `employee-api-stream-api`, `employee-api-stream-impl`)

lazy val `employee-api-api` = (project in file("employee-api-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `employee-api-impl` = (project in file("employee-api-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest,
      "com.typesafe.slick" %% "slick" % "3.3.3",
      "org.postgresql" % "postgresql" % "42.3.4",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
      "com.github.tminglei" %% "slick-pg" % "0.20.3",
      "com.github.tminglei" %% "slick-pg_play-json" % "0.20.3"
    )
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`employee-api-api`)

lazy val `employee-api-stream-api` = (project in file("employee-api-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `employee-api-stream-impl` = (project in file("employee-api-stream-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`employee-api-stream-api`, `employee-api-api`)

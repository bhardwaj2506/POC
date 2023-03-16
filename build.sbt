ThisBuild / organization := "squareoneinsights"
ThisBuild / version := "1.0"

ThisBuild / scalaVersion := "2.13.8"

ThisBuild / libraryDependencySchemes +=
  "org.scala-lang.modules" %% "scala-java8-compat" % VersionScheme.Always

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test
val alpakkaCassandra = "com.lightbend.akka"   %% "akka-stream-alpakka-cassandra" % "2.0.2"
val slick = "com.typesafe.slick" %% "slick" % "3.3.3"
val postgresql = "org.postgresql" % "postgresql" % "42.3.4"
val slickHikaricp = "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3"
val slickpg = "com.github.tminglei" %% "slick-pg" % "0.20.3"
val playJson = "com.github.tminglei" %% "slick-pg_play-json" % "0.20.3"
val metricsCore = "com.codahale.metrics" % "metrics-core" % "3.0.2"

lazy val `employee-api` = (project in file("."))
  .aggregate(`employee-api-api`, `employee-api-impl`)

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
      slick,      postgresql,
      slickHikaricp,
      slickpg,
      playJson,
      metricsCore,
      alpakkaCassandra
    )
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`employee-api-api`)

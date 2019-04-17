name := """solid.vip"""
organization := "vip.solid"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
scalacOptions ++= Seq("-deprecation", "-language:_")

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.1" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "vip.solid.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "vip.solid.binders._"

libraryDependencies += "org.reactivemongo" % "play2-reactivemongo_2.12" % "0.16.5-play27"
libraryDependencies += "com.typesafe.play" %% "play-mailer" % "6.0.0"

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

libraryDependencies += "com.github.jsonld-java" % "jsonld-java" % "0.12.3"
libraryDependencies += "org.apache.jena" % "apache-jena-libs" % "3.10.0"
libraryDependencies += "com.ironhorsesoftware" % "webid-tls-jsse" % "0.1.1"

libraryDependencies += "io.minio" % "minio" % "6.0.6"

libraryDependencies += "org.w3" % "ldp-testsuite" % "0.1.1" % Test // http://w3c.github.io/ldp-testsuite/

resolvers += (
  "Local Maven Repository" at s"file:///${Path.userHome.absolutePath}/.m2/repository"
)
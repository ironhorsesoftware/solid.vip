name := """solid.vip"""
organization := "vip.solid"

version := "1.0-SNAPSHOT"

lazy val silhouetteVersion = "6.0.0"
lazy val playSlickVersion = "3.0.0"

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

libraryDependencies += "io.minio" % "minio" % "6.0.6"

libraryDependencies ++= Seq(
    "com.mohiva"          %% "play-silhouette"                 % silhouetteVersion,
    "com.mohiva"          %% "play-silhouette-persistence"     % silhouetteVersion,
    "com.mohiva"          %% "play-silhouette-password-bcrypt" % silhouetteVersion,
    "com.mohiva"          %% "play-silhouette-crypto-jca"      % silhouetteVersion,
    "com.mohiva"          %% "play-silhouette-testkit"         % silhouetteVersion % "test",
    "com.typesafe.play"   %% "play-slick"                      % playSlickVersion,
    "com.typesafe.play"   %% "play-slick-evolutions"           % playSlickVersion,
    "com.iheart"          %% "ficus"                           % "1.4.3",
    "net.codingwell"      %% "scala-guice"                     % "4.1.0",
    "com.adrianhurt"      %% "play-bootstrap"                  % "1.5-P27-B4-RC-SNAPSHOT",
    "org.postgresql"       % "postgresql"                      % "42.2.5",
    guice,
    caffeine,
    evolutions
)


resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
updateOptions := updateOptions.value.withLatestSnapshots(false)
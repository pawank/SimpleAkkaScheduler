name := """SimpleScheduler"""

version := "1.0"

scalaVersion := "2.11.2"

crossScalaVersions  := Seq("2.11.2", "2.11.1", "2.10.4")

val commonlibraryDependencies = Seq(
  //"org.apache.httpcomponents" % "httpclient" % "4.3.6",
  "commons-httpclient" % "commons-httpclient" % "3.1",
  "org.scalatest" %% "scalatest" % "2.1.6" % "test" withSources,
  "com.typesafe" % "config" % "1.2.1" withSources
)

val loglibraryDependencies = commonlibraryDependencies ++ Seq(
  "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.0" withSources,
  "com.typesafe.akka" %% "akka-actor" % "2.3.4" withSources,
  "com.typesafe.akka" %% "akka-remote" % "2.3.4" withSources
)

seq(bintrayResolverSettings:_*)

lazy val root = (project in file(".")).settings(libraryDependencies ++= loglibraryDependencies)


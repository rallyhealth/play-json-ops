
name := "play-json-ops"

organization := "me.jeffmay"

version := "0.2.0"

// TODO: Cross-compile

scalaVersion := "2.10.4"

lazy val playVersion = settingKey[String]("The version of Play Json")

playVersion := "2.3.1"

libraryDependencies := Seq(
  "com.typesafe.play" %% "play-json" % playVersion.value,
  "org.scalacheck" %% "scalacheck" % "1.12.2" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
).map(_.withSources())


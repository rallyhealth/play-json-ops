name := "play-json-ops-root"

val Scala_2_11 = "2.11.11"

ThisBuild / scalaVersion := Scala_2_11

ThisBuild / organization := "com.rallyhealth"
ThisBuild / organizationName := "Rally Health"

ThisBuild / versionScheme := Some("early-semver")
ThisBuild / licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

ThisBuild / homepage := Some(url("https://github.com/rallyhealth/play-json-ops"))
ThisBuild / developers := List(
  Developer(id = "jeffmay", name = "Jeff May", email = "jeff.n.may@gmail.com", url = url("https://github.com/jeffmay")),
)

ThisBuild / resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"

// reload sbt when the build files change
Global / onChangedBuildSource := ReloadOnSourceChanges

// don't publish the aggregate root project
publish / skip := true

// don't search for previous artifact of the root project
ThisBuild / mimaFailOnNoPrevious := false

def commonProject(id: String): Project = {
  Project(id, file(id)).settings(
    name := id,

    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-deprecation:false",
      "-feature",
      "-Ywarn-dead-code",
      "-encoding", "UTF-8"
    ),

    // don't publish the test code as an artifact anymore, since we have playJsonTests
    Test / publishArtifact := false,

    // disable compilation of ScalaDocs, since this always breaks on links
    Compile / doc / sources := Seq.empty,

    // disable publishing empty ScalaDocs
    Compile / packageDoc / publishArtifact := false
  )
}

def playJsonOps(includePlayVersion: String): Project = {
  val playSuffix = includePlayVersion match {
    case Dependencies.play23Version => "23"
    case Dependencies.play25Version => "25"
  }
  val id = s"play$playSuffix-json-ops"
  commonProject(id).settings(
    libraryDependencies ++= Seq(
      Dependencies.playJson(includePlayVersion)
    )
  )
}

lazy val `play23-json-ops` = playJsonOps(Dependencies.play23Version)
lazy val `play25-json-ops` = playJsonOps(Dependencies.play25Version)

def playJsonTests(includePlayVersion: String, includeScalatestVersion: String): Project = {
  val playSuffix = includePlayVersion match {
    case Dependencies.play23Version => "23"
    case Dependencies.play25Version => "25"
  }
  val scalacheckSuffix = includeScalatestVersion match {
    case Dependencies.scalatest2Version => "-sc12"
    case Dependencies.scalatest3Version => ""
  }
  val id = s"play$playSuffix-json-tests$scalacheckSuffix"
  val projectPath = s"play$playSuffix-json-tests"
  commonProject(id).settings(
    // set the source code directories to the shared project root
    (Compile / sourceDirectory) := file(s"$projectPath/src/main").getAbsoluteFile,
    (Test / sourceDirectory) := file(s"$projectPath/src/test").getAbsoluteFile,
    libraryDependencies ++= Seq(
      Dependencies.scalatest(includeScalatestVersion),
      Dependencies.scalacheckOps(includeScalatestVersion)
    )
  ).dependsOn((includePlayVersion match {
    case Dependencies.play23Version => Seq(
      `play23-json-ops`
    )
    case Dependencies.play25Version => Seq(
      `play25-json-ops`
    )
  }).map(_ % Compile): _*)
}

lazy val `play23-json-tests-sc12` = playJsonTests(Dependencies.play23Version, Dependencies.scalatest2Version)
lazy val `play23-json-tests` = playJsonTests(Dependencies.play23Version, Dependencies.scalatest3Version)
lazy val `play25-json-tests-sc12` = playJsonTests(Dependencies.play25Version, Dependencies.scalatest2Version)
lazy val `play25-json-tests` = playJsonTests(Dependencies.play25Version, Dependencies.scalatest3Version)


lazy val commonRootSettings = Seq(
  version := "1.5.0",
  scalaVersion := "2.11.8",
  organization := "me.jeffmay",
  organizationName := "Jeff May"
)

lazy val root = Project("play-json-ops-root", file("."))
  .aggregate(`play-json-ops-23`, `play-json-tests-23`, `play-json-ops-25`, `play-json-tests-25`)
  .settings(commonRootSettings ++ Seq(
    // don't publish the surrounding multi-project build
    publish := {},
    publishLocal := {}
  ))

val PlayJsonVersion = new {
  val _23 = "2.3.10"
  val _25 = "2.5.10"
}

lazy val common = commonRootSettings ++ Seq(

  scalacOptions ++= {
    // the deprecation:false flag is only supported by scala >= 2.11.3, but needed for scala >= 2.11.0 to avoid warnings
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, scalaMinor)) if scalaMinor >= 11 =>
        // For scala versions >= 2.11.3
        Seq("-Xfatal-warnings", "-deprecation:false")
      case Some((2, scalaMinor)) if scalaMinor < 11 =>
        // For scala versions 2.10.x
        Seq("-Xfatal-warnings", "-deprecation")
    }
  } ++ Seq(
    "-feature",
    "-Ywarn-dead-code",
    "-encoding", "UTF-8"
  ),

  resolvers ++= Seq(
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
    "jeffmay at bintray" at "https://dl.bintray.com/jeffmay/maven"
  ),

  ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },

  // don't publish the test code as an artifact anymore, since we have playJsonTests
  publishArtifact in Test := false,

  // disable compilation of ScalaDocs, since this always breaks on links
  sources in(Compile, doc) := Seq.empty,

  // disable publishing empty ScalaDocs
  publishArtifact in (Compile, packageDoc) := false,

  licenses += ("Apache-2.0", url("http://opensource.org/licenses/apache-2.0"))

)

def playJsonOpsCommon(playVersion: String) = common ++ Seq(
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json" % playVersion
  )
)

lazy val `play-json-ops-23` = project in file("play-json-ops-23") settings (
  playJsonOpsCommon(PlayJsonVersion._23),
  name := "play-json-ops",
  crossScalaVersions := Seq("2.11.8", "2.10.6")
)

lazy val `play-json-ops-25` = project in file("play-json-ops-25") settings (
  playJsonOpsCommon(PlayJsonVersion._25),
  name := "play-json-ops-25"
)

def playJsonTestsCommon(playVersion: String) = common ++ Seq(
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json" % playVersion,
    "org.scalacheck" %% "scalacheck" % "1.12.5",
    "org.scalatest" %% "scalatest" % "2.2.6",
    "me.jeffmay" %% "scalacheck-ops" % "1.5.0"
  )
)

lazy val `play-json-tests-23` = project in file("play-json-tests-23") settings (
  playJsonTestsCommon(PlayJsonVersion._23),
  name := "play-json-tests",
  crossScalaVersions := Seq("2.11.8", "2.10.6")
) dependsOn `play-json-ops-23`

lazy val `play-json-tests-25` = project in file("play-json-tests-25") settings(common: _*) settings (
  playJsonTestsCommon(PlayJsonVersion._25),
  name := "play-json-tests-25"
) dependsOn `play-json-ops-25`

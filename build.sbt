
lazy val commonRootSettings = Seq(
  version := "1.1.1",
  scalaVersion := "2.11.6",
  crossScalaVersions := Seq("2.11.6", "2.10.4"),
  organization := "me.jeffmay",
  organizationName := "Jeff May"
) ++ bintraySettings ++ bintrayPublishSettings

commonRootSettings

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

lazy val playJsonOps = project in file("playJsonOps") settings(common: _*) settings (

  name := "play-json-ops",

  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json" % "2.3.7"
  )

) dependsOn (
  playJsonTests % "test"
)

lazy val playJsonTests = project in file("playJsonTests") settings(common: _*) settings (

  name := "play-json-tests",

  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json" % "2.3.7",
    "org.scalacheck" %% "scalacheck" % "1.12.2",
    "org.scalatest" %% "scalatest" % "2.2.4",
    "me.jeffmay" %% "scalacheck-ops" % "1.0.0"
  )
)

// don't publish the surrounding multi-project build
publish := {}


name := "play-json-ops"

organization := "me.jeffmay"

organizationName := "Jeff May"

version := "1.0.0"

crossScalaVersions := Seq("2.11.6", "2.10.4")

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
)

libraryDependencies := Seq(
  "com.typesafe.play" %% "play-json" % "2.3.7",
  // these are not limited to the test scope since there is library code that enables free unit tests
  // when extending a generic test class for PlaySerializationTests
  "org.scalacheck" %% "scalacheck" % "1.12.2",
  "org.scalatest" %% "scalatest" % "2.2.4",
  "me.jeffmay" %% "scalacheck-ops" % "1.0.0"
).map(_.withSources())

// disable compilation of ScalaDocs, since this always breaks on links
sources in(Compile, doc) := Seq.empty

// disable publishing empty ScalaDocs
publishArtifact in (Compile, packageDoc) := false

bintraySettings ++ bintrayPublishSettings

licenses += ("Apache-2.0", url("http://opensource.org/licenses/apache-2.0"))

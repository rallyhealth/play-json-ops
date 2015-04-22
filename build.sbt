
name := "play-json-ops"

organization := "me.jeffmay"

version := "0.2.4"

crossScalaVersions := Seq("2.10.4", "2.11.6")

scalacOptions := {
  // the deprecation:false flag is only supported by scala >= 2.11.3, but needed for scala >= 2.11.0 to avoid warnings
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, scalaMinor)) if scalaMinor >= 11 =>
      // For scala versions >= 2.11.3
      Seq("-Xfatal-warnings", "-deprecation:false")
    case Some((2, scalaMinor)) if scalaMinor < 11 =>
      // For scala versions 2.10.x
      Seq("-Xfatal-warnings")
  }
}

resolvers := Seq(
  sbt.Resolver.jcenterRepo,
  sbt.Resolver.bintrayRepo("jeffmay", "typesafety")
)

libraryDependencies := Seq(
  "com.typesafe.play" %% "play-json" % "2.3.7",
  // these are not limited to the test scope since there is library code that enables free unit tests
  // when extending a generic test class for PlaySerializationTests
  "org.scalacheck" %% "scalacheck" % "1.12.2",
  "org.scalatest" %% "scalatest" % "2.2.4",
  "me.jeffmay" %% "scalacheck-ops" % "0.1.1"
).map(_.withSources())

bintraySettings ++ bintrayPublishSettings

licenses += ("Apache-2.0", url("http://opensource.org/licenses/apache-2.0"))

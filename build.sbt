name := "play-json-ops-root"
organization in ThisBuild := "me.jeffmay"
organizationName in ThisBuild := "Jeff May"

version in ThisBuild := "1.6.1"
scalaVersion in ThisBuild := "2.11.11"

// don't publish the surrounding multi-project build
publish := {}
publishLocal := {}

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

    resolvers ++= Seq(
      "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
      "jeffmay at bintray" at "https://dl.bintray.com/jeffmay/maven"
    ),

    ivyScala := ivyScala.value map {
      _.copy(overrideScalaVersion = true)
    },

    // don't publish the test code as an artifact anymore, since we have playJsonTests
    publishArtifact in Test := false,

    // disable compilation of ScalaDocs, since this always breaks on links
    sources in(Compile, doc) := Seq.empty,

    // disable publishing empty ScalaDocs
    publishArtifact in(Compile, packageDoc) := false,

    licenses += ("Apache-2.0", url("http://opensource.org/licenses/apache-2.0"))

  )
}

def playJsonOps(includePlayVersion: String): Project = {
  val playSuffix = includePlayVersion match {
    case Dependencies.play23Version => "23"
    case Dependencies.play25Version => "25"
  }
  val legacySuffix = includePlayVersion match {
    case Dependencies.play23Version => ""
    case Dependencies.play25Version => "-25"
  }
  val id = s"play$playSuffix-json-ops"
  commonProject(id).settings(
    // support legacy artifact name for 1.x branch final release
    name := s"play-json-ops$legacySuffix",
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
  val legacySuffix = includePlayVersion match {
    case Dependencies.play23Version => ""
    case Dependencies.play25Version => "-25"
  }
  val scalacheckSuffix = includeScalatestVersion match {
    case Dependencies.scalatest2Version => "-sc12"
    case Dependencies.scalatest3Version => ""
  }
  val id = s"play$playSuffix-json-tests$scalacheckSuffix"
  val projectPath = s"play$playSuffix-json-tests"
  commonProject(id).settings(
    // support legacy artifact name for 1.x branch final release
    name := (includeScalatestVersion match {
      case Dependencies.scalatest2Version => s"play-json-tests$legacySuffix"
      case Dependencies.scalatest3Version => id
    }),
    // set the source code directories to the shared project root
    sourceDirectory := file(s"$projectPath/src").getAbsoluteFile,
    (sourceDirectory in Compile) := file(s"$projectPath/src/main").getAbsoluteFile,
    (sourceDirectory in Test) := file(s"$projectPath/src/test").getAbsoluteFile,
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

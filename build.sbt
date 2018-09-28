name := "play-json-ops-root"
organization in ThisBuild := "com.rallyhealth"
organizationName in ThisBuild := "Rally Health"

scalaVersion in ThisBuild := "2.11.11"

bintrayOrganization in ThisBuild := Some("rallyhealth")
bintrayRepository in ThisBuild := "maven"

licenses in ThisBuild := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

resolvers in ThisBuild += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
resolvers in ThisBuild += Resolver.bintrayRepo("rallyhealth", "maven")

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

    ivyScala := ivyScala.value map {
      _.copy(overrideScalaVersion = true)
    },

    // don't publish the test code as an artifact anymore, since we have playJsonTests
    publishArtifact in Test := false,

    // disable compilation of ScalaDocs, since this always breaks on links
    sources in(Compile, doc) := Seq.empty,

    // disable publishing empty ScalaDocs
    publishArtifact in(Compile, packageDoc) := false

  ).enablePlugins(SemVerPlugin)
}

def playJsonOps(includePlayVersion: String): Project = {
  val playSuffix = includePlayVersion match {
    case Dependencies.play23Version => "23"
    case Dependencies.play25Version => "25"
  }
  val scalatestVersion = includePlayVersion match {
    case Dependencies.play23Version => Dependencies.scalatest2Version
    case Dependencies.play25Version => Dependencies.scalatest3Version
  }
  val id = s"play$playSuffix-json-ops"
  commonProject(id).settings(
    libraryDependencies ++= Seq(
      Dependencies.playJson(includePlayVersion)
    ) ++ Seq(
      Dependencies.scalacheckOps(scalatestVersion),
      Dependencies.scalatest(scalatestVersion)
    ).map(_ % Test)
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

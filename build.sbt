import Dependencies._

name := "play-json-ops-root"
ThisBuild / organization := "com.rallyhealth"
ThisBuild / organizationName := "Rally Health"

val Scala_2_11 = "2.11.12"
val Scala_2_12 = "2.12.6"
val Scala_2_13 = "2.13.0"

ThisBuild / gitVersioningSnapshotLowerBound := "3.0.0"

ThisBuild / bintrayOrganization := Some("rallyhealth")
ThisBuild / bintrayRepository := "maven"

ThisBuild / licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

ThisBuild / resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
ThisBuild / resolvers += Resolver.bintrayRepo("rallyhealth", "maven")

// don't publish the surrounding multi-project build
publish := {}
publishLocal := {}

def commonProject(id: String): Project = {
  Project(id, file(id)).settings(
    name := id,

    // Include all of the dependencies in the loader. The base loader will be the Application
    // ClassLoader. All classes apart from system classes will be reloaded with each run instead
    // of being cached between layers.
    classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,

    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Xfatal-warnings",
      "-Ywarn-dead-code",
      "-encoding", "UTF-8"
    ),

    // don't publish the test code as an artifact anymore, since we have playJsonTests
    Test / publishArtifact := false,

    // disable compilation of ScalaDocs, since this always breaks on links
    Compile / doc / sources := Seq.empty,

    // disable publishing empty ScalaDocs
    Compile / packageDoc / publishArtifact := false

  ).enablePlugins(SemVerPlugin)
}

def playSuffix(includePlayVersion: String): String = includePlayVersion match {
  case Play_2_5 => "25"
  case Play_2_6 => "26"
  case Play_2_7 => "27"
}

def scalaVersions(includePlayVersion: String): Seq[String] = includePlayVersion match {
  case Play_2_5 => Seq(Scala_2_11)
  case Play_2_6 => Seq(Scala_2_12, Scala_2_11)
  case Play_2_7 => Seq(Scala_2_13)
}

def scalaCheckVersionForPlay(includePlayVersion: String): String = includePlayVersion match {
  case Play_2_5 => ScalaCheck_1_12
  case Play_2_6 => ScalaCheck_1_13
  case Play_2_7 => ScalaCheck_1_14
}

def playJsonOpsCommon(includePlayVersion: String): Project = {
  val id = s"play${playSuffix(includePlayVersion)}-json-ops-common"
  val projectPath = "play-json-ops-common"
  val versions = scalaVersions(includePlayVersion)
  val scalaCheckVersion = scalaCheckVersionForPlay(includePlayVersion)
  commonProject(id).settings(
    scalaVersion := versions.head,
    crossScalaVersions := versions,
    // set the source code directories to the shared project root
    sourceDirectory := file(s"$projectPath/src").getAbsoluteFile,
    Compile / sourceDirectory := file(s"$projectPath/src/main").getAbsoluteFile,
    Test / sourceDirectory := file(s"$projectPath/src/test").getAbsoluteFile,
    libraryDependencies ++= Seq(
      playJson(includePlayVersion)
    ) ++ Seq(
      // Test-only dependencies
      scalaTest(scalaCheckVersion)
    ).map(_ % Test)
  )
}

lazy val `play25-json-ops-common` = playJsonOpsCommon(Play_2_5)
lazy val `play26-json-ops-common` = playJsonOpsCommon(Play_2_6)

def playJsonOps(includePlayVersion: String): Project = {
  val id = s"play${playSuffix(includePlayVersion)}-json-ops"
  val versions = scalaVersions(includePlayVersion)
  val scalaCheckVersion = scalaCheckVersionForPlay(includePlayVersion)
  commonProject(id)
    .settings(
      scalaVersion := versions.head,
      crossScalaVersions := versions,
      libraryDependencies ++= Seq(
        playJson(includePlayVersion)
      ) ++ Seq(
        // Test-only dependencies
        scalaTest(scalaCheckVersion)
      ).map(_ % Test)
    )
    .dependsOn(includePlayVersion match {
      case Play_2_5 => `play25-json-ops-common`
      case Play_2_6 => `play26-json-ops-common`
    })
}

lazy val `play25-json-ops` = playJsonOps(Play_2_5).settings(
  libraryDependencies += scalaCheckOps(ScalaCheck_1_12) % Test
)
lazy val `play26-json-ops` = playJsonOps(Play_2_6)
lazy val `play27-json-ops` = {
  val versions = scalaVersions(Play_2_7)
  commonProject("play27-json-ops").settings(
    scalaVersion := versions.head,
    crossScalaVersions := versions,
    scalacOptions += "-deprecation:false",
    libraryDependencies ++= Seq(
      playJson(Play_2_7)
    ) ++ Seq(
      // Test-only dependencies
      scalaTest(ScalaCheck_1_14),
      scalaTestPlusScalaCheck(ScalaCheck_1_14),
      scalaCheckOps(ScalaCheck_1_14)
    ).map(_ % Test)
  )
}

def playJsonTests(includePlayVersion: String, includeScalaCheckVersion: String): Project = {
  val scalacheckSuffix = includeScalaCheckVersion match {
    case ScalaCheck_1_12 => "-sc12"
    case ScalaCheck_1_13 => "-sc13"
  }
  val id = s"play${playSuffix(includePlayVersion)}-json-tests$scalacheckSuffix"
  val projectPath = "play-json-tests-common"
  val versions = scalaVersions(includePlayVersion)
  commonProject(id).settings(
    scalaVersion := versions.head,
    crossScalaVersions := versions,
    // set the source code directories to the shared project root
    sourceDirectory := file(s"$projectPath/src").getAbsoluteFile,
    Compile / sourceDirectory := file(s"$projectPath/src/main").getAbsoluteFile,
    Test / sourceDirectory := file(s"$projectPath/src/test").getAbsoluteFile,
    libraryDependencies ++= Seq(
      scalaTest(includeScalaCheckVersion),
      scalaCheckOps(includeScalaCheckVersion)
    )
  ).dependsOn((includePlayVersion match {
    case Play_2_5 => Seq(
      `play25-json-ops`
    )
    case Play_2_6 => Seq(
      `play26-json-ops`
    )
  }).map(_ % Compile): _*)
}

lazy val `play25-json-tests-sc12` = playJsonTests(Play_2_5, ScalaCheck_1_12)
lazy val `play25-json-tests-sc13` = playJsonTests(Play_2_5, ScalaCheck_1_13)
lazy val `play26-json-tests-sc13` = playJsonTests(Play_2_6, ScalaCheck_1_13)
lazy val `play27-json-tests-sc14` = {
  val versions = scalaVersions(Play_2_7)
  commonProject(s"play${playSuffix(Play_2_7)}-json-tests-sc14").settings(
    scalaVersion := versions.head,
    crossScalaVersions := versions,
    libraryDependencies ++= Seq(
      scalaCheckOps(ScalaCheck_1_14),
      scalaTest(ScalaCheck_1_14),
      scalaTestPlusScalaCheck(ScalaCheck_1_14)
    )
  ).dependsOn(`play27-json-ops`)
}

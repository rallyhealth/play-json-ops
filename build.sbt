import Dependencies._

name := "play-json-ops-root"
ThisBuild / organization := "com.rallyhealth"
ThisBuild / organizationName := "Rally Health"

ThisBuild / bintrayOrganization := Some("rallyhealth")
ThisBuild / bintrayRepository := "maven"

scalaVersion := Scala_2_13
ThisBuild / licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

ThisBuild / resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
ThisBuild / resolvers += Resolver.bintrayRepo("rallyhealth", "maven")

// reload sbt when the build files change
Global / onChangedBuildSource := ReloadOnSourceChanges

// don't publish the aggregate root project
publish / skip := true
publishLocal / skip := true

// don't search for previous artifact of the root project
mimaFailOnNoPrevious := false

def commonProject(id: String, projectPath: String, scalacVersion: String): Project = {
  val versionSuffix = scalacVersion match {
    case Scala_2_11 => "211"
    case Scala_2_12 => "212"
    case Scala_2_13 => "213"
  }
  val target = s"$id-$versionSuffix"
  Project(target, file(target)).settings(
    name := {
      // Shade only non-test jars since they're most exposed to transitive dependency hell.
      def majorVersion = version.value.split('.').head
      if (id.contains("test")) id else s"$id-v$majorVersion"
    },

    // set the Scala version to the given version
    scalaVersion := scalacVersion,

    // verify binary compatibility
    mimaPreviousArtifacts := Set(organization.value %% name.value % "4.2.0"),

    // set the source code directories to the shared project root
    Compile / sourceDirectory := file(s"$projectPath/src/main").getAbsoluteFile,
    Test / sourceDirectory := file(s"$projectPath/src/test").getAbsoluteFile,

    // Include all of the dependencies in the loader. The base loader will be the Application
    // ClassLoader. All classes apart from system classes will be reloaded with each run instead
    // of being cached between layers.
    classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,

    scalacOptions ++= Seq(
      "-deprecation:false",
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

  )
}

def playSuffix(includePlayVersion: String): String = includePlayVersion match {
  case Play_2_5 => "25"
  case Play_2_6 => "26"
  case Play_2_7 => "27"
  case Play_2_8 => "28"
}

def scalaCheckVersionForPlay(includePlayVersion: String): String = includePlayVersion match {
  case Play_2_5 => ScalaCheck_1_12
  case Play_2_6 => ScalaCheck_1_13
  case Play_2_7 => ScalaCheck_1_14
  case Play_2_8 => ScalaCheck_1_14
}

def playJsonOpsCommon(scalacVersion: String, includePlayVersion: String): Project = {
  val id = s"play${playSuffix(includePlayVersion)}-json-ops-common"
  val projectPath = scalacVersion match {
    case Scala_2_13 => "play-json-ops-common-213"
    case _ => "play-json-ops-common"
  }
  commonProject(id, projectPath, scalacVersion).settings(
    libraryDependencies ++= Seq(
      playJson(includePlayVersion)
    )
  )
}

lazy val `play25-json-ops-common-211` = playJsonOpsCommon(Scala_2_11, Play_2_5)
lazy val `play26-json-ops-common-211` = playJsonOpsCommon(Scala_2_11, Play_2_6)
lazy val `play26-json-ops-common-212` = playJsonOpsCommon(Scala_2_12, Play_2_6)
lazy val `play27-json-ops-common-211` = playJsonOpsCommon(Scala_2_11, Play_2_7)
lazy val `play27-json-ops-common-212` = playJsonOpsCommon(Scala_2_12, Play_2_7)

// TODO: Use play27-json-ops-common-213 in the next major version
// For binary compatibility, we must keep a copy of the original code of play27-json-ops,
// and cannot use this common project
// lazy val `play27-json-ops-common-213` = playJsonOpsCommon(Scala_2_13, Play_2_7)

lazy val `play28-json-ops-common-212` = playJsonOpsCommon(Scala_2_12, Play_2_8)
  .settings( // ignore binary compatibility checking until we have a previous release artifact for Play 2.8
    mimaPreviousArtifacts := Set(),
    mimaFailOnNoPrevious := false,
  )
lazy val `play28-json-ops-common-213` = playJsonOpsCommon(Scala_2_13, Play_2_8)
  .settings( // ignore binary compatibility checking until we have a previous release artifact for Play 2.8
    mimaPreviousArtifacts := Set(),
    mimaFailOnNoPrevious := false,
  )

def playJsonOps(scalacVersion: String, includePlayVersion: String): Project = {
  val id = s"play${playSuffix(includePlayVersion)}-json-ops"
  val projectPath = (includePlayVersion, scalacVersion) match {
    // This project was created with its own source code, rather than sharing a common project
    case (Play_2_7, Scala_2_13) => s"play${playSuffix(includePlayVersion)}-json-ops-scala213"
    case _ => id
  }
  val scalaCheckVersion = scalaCheckVersionForPlay(includePlayVersion)
  commonProject(id, projectPath, scalacVersion)
    .settings(
      libraryDependencies ++= Seq(
        playJson(includePlayVersion)
      ) ++ {
        // Test-only dependencies
        Seq(
          scalaCheckOps(scalaCheckVersion),
          scalaTest(scalaCheckVersion)
        ) ++ scalaTestPlusScalaCheck(scalaCheckVersion)
      }.map(_ % Test),
    )
    .dependsOn(((scalacVersion, includePlayVersion) match {
      case (Scala_2_11, Play_2_5) => Seq(
        `play25-json-ops-common-211`
      )
      case (Scala_2_11, Play_2_6) => Seq(
        `play26-json-ops-common-211`
      )
      case (Scala_2_12, Play_2_6) => Seq(
        `play26-json-ops-common-212`
      )
      case (Scala_2_11, Play_2_7) => Seq(
        `play27-json-ops-common-211`
      )
      case (Scala_2_12, Play_2_7) => Seq(
        `play27-json-ops-common-212`
      )
      case (Scala_2_13, Play_2_7) => Seq(
        // For binary compatibility, we must keep a copy of the original code, and cannot use the common project
        // TODO: Use play27-json-ops-common-213 in the next major version
      )
      case (Scala_2_12, Play_2_8) => Seq(
        `play28-json-ops-common-212`
      )
      case (Scala_2_13, Play_2_8) => Seq(
        `play28-json-ops-common-213`
      )
    }).map(_ % Compile): _*)
}

lazy val `play25-json-ops-211` = playJsonOps(Scala_2_11, Play_2_5).settings(
  libraryDependencies += scalaCheckOps(ScalaCheck_1_12) % Test
)
lazy val `play26-json-ops-211` = playJsonOps(Scala_2_11, Play_2_6)
lazy val `play26-json-ops-212` = playJsonOps(Scala_2_12, Play_2_6)
lazy val `play27-json-ops-211` = playJsonOps(Scala_2_11, Play_2_7)
lazy val `play27-json-ops-212` = playJsonOps(Scala_2_12, Play_2_7)
lazy val `play27-json-ops-213` = playJsonOps(Scala_2_13, Play_2_7)
lazy val `play28-json-ops-212` = playJsonOps(Scala_2_12, Play_2_8)
lazy val `play28-json-ops-213` = playJsonOps(Scala_2_13, Play_2_8)

def playJsonTests(scalacVersion: String, includePlayVersion: String, includeScalaCheckVersion: String): Project = {
  val scalaCheckSuffix = includeScalaCheckVersion match {
    case ScalaCheck_1_12 => "-sc12"
    case ScalaCheck_1_13 => "-sc13"
    case ScalaCheck_1_14 => "-sc14"
  }
  val id = s"play${playSuffix(includePlayVersion)}-json-tests$scalaCheckSuffix"
  val projectPath = (includePlayVersion, includeScalaCheckVersion) match {
    // Scala 2.13 and ScalaTest 3.1 has some source code incompatibilities that require separate source directories
    case (Play_2_5, ScalaCheck_1_14) => "play25-json-tests-sc14"
    case (Play_2_7, ScalaCheck_1_14) => "play27-json-tests-sc14"
    case (Play_2_8, ScalaCheck_1_14) => "play28-json-tests-sc14"
    case _ => "play-json-tests-common"
  }
  commonProject(id, projectPath, scalacVersion).settings(
    Test / scalacOptions -= "-deprecation",

    libraryDependencies ++= {
      Seq(
        scalaCheckOps(includeScalaCheckVersion),
        scalaTest(includeScalaCheckVersion),
      ) ++ scalaParallelCollections(scalacVersion)
    } ++ {
      // Test-only dependencies
      scalaTestPlusScalaCheck(includeScalaCheckVersion)
    }.map(_ % Test)
  ).dependsOn(((scalacVersion, includePlayVersion) match {
    case (Scala_2_11, Play_2_5) => Seq(
      `play25-json-ops-211`
    )
    case (Scala_2_11, Play_2_6) => Seq(
      `play26-json-ops-211`
    )
    case (Scala_2_12, Play_2_6) => Seq(
      `play26-json-ops-212`
    )
    case (Scala_2_11, Play_2_7) => Seq(
      `play27-json-ops-211`
    )
    case (Scala_2_12, Play_2_7) => Seq(
      `play27-json-ops-212`
    )
    case (Scala_2_13, Play_2_7) => Seq(
      `play27-json-ops-213`
    )
    case (Scala_2_12, Play_2_8) => Seq(
      `play28-json-ops-212`
    )
    case (Scala_2_13, Play_2_8) => Seq(
      `play28-json-ops-213`
    )
  }).map(_ % Compile): _*)
}

lazy val `play25-json-tests-sc12-211` = playJsonTests(Scala_2_11, Play_2_5, ScalaCheck_1_12)
lazy val `play25-json-tests-sc13-211` = playJsonTests(Scala_2_11, Play_2_5, ScalaCheck_1_13)
lazy val `play25-json-tests-sc14-211` = playJsonTests(Scala_2_11, Play_2_5, ScalaCheck_1_14)
lazy val `play26-json-tests-sc13-211` = playJsonTests(Scala_2_11, Play_2_6, ScalaCheck_1_13)
lazy val `play26-json-tests-sc13-212` = playJsonTests(Scala_2_12, Play_2_6, ScalaCheck_1_13)
lazy val `play27-json-tests-sc13-211` = playJsonTests(Scala_2_11, Play_2_7, ScalaCheck_1_14)
lazy val `play27-json-tests-sc13-212` = playJsonTests(Scala_2_12, Play_2_7, ScalaCheck_1_14)
lazy val `play27-json-tests-sc14-213` = playJsonTests(Scala_2_13, Play_2_7, ScalaCheck_1_14)
lazy val `play28-json-tests-sc13-212` = playJsonTests(Scala_2_12, Play_2_8, ScalaCheck_1_14)
lazy val `play28-json-tests-sc14-213` = playJsonTests(Scala_2_13, Play_2_8, ScalaCheck_1_14)

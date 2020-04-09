import Dependencies._

name := "play-json-ops-root"
ThisBuild / organization := "com.rallyhealth"
ThisBuild / organizationName := "Rally Health"

ThisBuild / gitVersioningSnapshotLowerBound := "3.0.0"

ThisBuild / bintrayOrganization := Some("rallyhealth")
ThisBuild / bintrayRepository := "maven"

ThisBuild / licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

ThisBuild / resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
ThisBuild / resolvers += Resolver.bintrayRepo("rallyhealth", "maven")

// don't publish the surrounding multi-project build
publish := {}
publishLocal := {}

def commonProject(id: String, projectPath: String, scalacVersion: String): Project = {
  val versionSuffix = scalacVersion match {
    case Scala_2_11 => "211"
    case Scala_2_12 => "212"
    case Scala_2_13 => "213"
  }
  val target = s"$id-$versionSuffix"
  Project(target, file(target)).settings(
    name := id,

    scalaVersion := scalacVersion,

    // set the source code directories to the shared project root
    sourceDirectory := file(s"$projectPath/src").getAbsoluteFile,
    Compile / sourceDirectory := file(s"$projectPath/src/main").getAbsoluteFile,
    Test / sourceDirectory := file(s"$projectPath/src/test").getAbsoluteFile,

    // Suppress semver check for libraries that were missed
    semVerEnforceAfterVersion := Some("3.2.0"),

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

  ).enablePlugins(SemVerPlugin)
}

def playSuffix(includePlayVersion: String): String = includePlayVersion match {
  case Play_2_5 => "25"
  case Play_2_6 => "26"
  case Play_2_7 => "27"
}

def scalaCheckVersionForPlay(includePlayVersion: String): String = includePlayVersion match {
  case Play_2_5 => ScalaCheck_1_12
  case Play_2_6 => ScalaCheck_1_13
  case Play_2_7 => ScalaCheck_1_14
}

def playJsonOpsCommon(scalacVersion: String, includePlayVersion: String): Project = {
  val id = s"play${playSuffix(includePlayVersion)}-json-ops-common"
  val projectPath = "play-json-ops-common"
  val scalaCheckVersion = scalaCheckVersionForPlay(includePlayVersion)
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

def playJsonOps(scalacVersion: String, includePlayVersion: String): Project = {
  val id = s"play${playSuffix(includePlayVersion)}-json-ops"
  val projectPath = scalacVersion match {
    case Scala_2_13 => s"play${playSuffix(includePlayVersion)}-json-ops-scala213"
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
        ) ++ {
          scalaCheckVersion match {
            case ScalaCheck_1_14 => Seq(
              scalaTestPlusScalaCheck(scalaCheckVersion)
            )
            case _ => Seq()
          }
        }
      }.map(_ % Test)
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
      case _ => Seq()
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

def playJsonTests(scalacVersion: String, includePlayVersion: String, includeScalaCheckVersion: String): Project = {
  val scalaCheckSuffix = includeScalaCheckVersion match {
    case ScalaCheck_1_12 => "-sc12"
    case ScalaCheck_1_13 => "-sc13"
    case ScalaCheck_1_14 => "-sc14"
  }
  val id = s"play${playSuffix(includePlayVersion)}-json-tests$scalaCheckSuffix"
  val projectPath = (includePlayVersion, includeScalaCheckVersion) match {
    // Scala 2.13 and ScalaTest 3.1 has some source code incompatibilities that require separate source directories
    case (Play_2_7, ScalaCheck_1_14) => "play27-json-tests-sc14"
    case _ => "play-json-tests-common"
  }
  commonProject(id, projectPath, scalacVersion).settings(
    Test / scalacOptions -= "-deprecation",
    libraryDependencies ++= Seq(
      scalaCheckOps(includeScalaCheckVersion),
      scalaTest(includeScalaCheckVersion)
    ) ++ {
      // Test-only dependencies
      includeScalaCheckVersion match {
        case ScalaCheck_1_14 => Seq(
          scalaTestPlusScalaCheck(includeScalaCheckVersion)
        )
        case _ => Seq()
      }
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
    case _ => Seq()
  }).map(_ % Compile): _*)
}

lazy val `play25-json-tests-sc12-211` = playJsonTests(Scala_2_11, Play_2_5, ScalaCheck_1_12)
lazy val `play25-json-tests-sc13-211` = playJsonTests(Scala_2_11, Play_2_5, ScalaCheck_1_13)
lazy val `play26-json-tests-sc13-211` = playJsonTests(Scala_2_11, Play_2_6, ScalaCheck_1_13)
lazy val `play26-json-tests-sc13-212` = playJsonTests(Scala_2_12, Play_2_6, ScalaCheck_1_13)
lazy val `play27-json-tests-sc13-211` = playJsonTests(Scala_2_11, Play_2_7, ScalaCheck_1_14)
lazy val `play27-json-tests-sc13-212` = playJsonTests(Scala_2_12, Play_2_7, ScalaCheck_1_14)
lazy val `play27-json-tests-sc14-213` = playJsonTests(Scala_2_13, Play_2_7, ScalaCheck_1_14)

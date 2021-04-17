import sbt._

object Dependencies {

  final val Play_2_5 = "2.5.19"
  final val Play_2_6 = "2.6.25"
  final val Play_2_7 = "2.7.9"
  final val Play_2_8 = "2.8.7"

  final val Scala_2_11 = "2.11.12"
  final val Scala_2_12 = "2.12.12"
  final val Scala_2_13 = "2.13.5"

  final val ScalaCheck_1_12 = "1.12.6"
  final val ScalaCheck_1_13 = "1.13.5"
  final val ScalaCheck_1_14 = "1.14.3"

  private val Play_2_6_JsonVersion = "2.6.10"
  private val Play_2_7_JsonVersion = "2.7.4"
  private val Play_2_8_JsonVersion = "2.8.1"
  private val ScalaCheckOpsVersion = "2.6.0"
  private val ScalaParallelCollectionsVersion = "1.0.2"
  private val ScalaTest_2 = "2.2.6"
  private val ScalaTest_3 = "3.0.5"
  private val ScalaTest_3_2 = "3.2.7"
  private val ScalaTestPlusScalaCheckVersion = "3.2.0.0"

  def playJson(playVersion: String): ModuleID = {
    val playJsonVersion = playVersion match {
      case Play_2_5 => Play_2_5
      case Play_2_6 => Play_2_6_JsonVersion
      case Play_2_7 => Play_2_7_JsonVersion
      case Play_2_8 => Play_2_8_JsonVersion
    }
    "com.typesafe.play" %% "play-json" % playJsonVersion
  }

  def scalaCheckOps(scalaCheckVersion: String): ModuleID = {
    val suffix = scalaCheckVersion match {
      case ScalaCheck_1_12 => "_1-12"
      case ScalaCheck_1_13 => "_1-13"
      case ScalaCheck_1_14 => "_1-14"
    }
    "com.rallyhealth" %% s"scalacheck-ops$suffix" % ScalaCheckOpsVersion
  }

  def scalaParallelCollections(scalaVersion: String): Seq[ModuleID] = scalaVersion match {
    case Scala_2_13 => Seq(
      "org.scala-lang.modules" %% "scala-parallel-collections" % ScalaParallelCollectionsVersion
    )
    case _ => Seq()
  }

  def scalaTest(scalaCheckVersion: String): ModuleID = {
    val version = scalaCheckVersion match {
      case ScalaCheck_1_12 => ScalaTest_2
      case ScalaCheck_1_13 => ScalaTest_3
      case ScalaCheck_1_14 => ScalaTest_3_2
    }
    "org.scalatest" %% "scalatest" % version
  }

  def scalaTestPlusScalaCheck(scalaCheckVersion: String): Seq[ModuleID] = scalaCheckVersion match {
    case ScalaCheck_1_14 => Seq(
      "org.scalatestplus" %% "scalacheck-1-14" % ScalaTestPlusScalaCheckVersion
    )
    case _ => Seq()
  }

}

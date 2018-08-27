import sbt._

object Dependencies {

  val play23Version = "2.3.10"
  val play25Version = "2.5.10"

  val scalatest2Version = "2.2.6"
  val scalatest3Version = "3.0.4"

  private val scalacheckOpsVersion = "2.1.0"

  def playJson(playVersion: String): ModuleID = {
    "com.typesafe.play" %% "play-json" % playVersion
  }

  def scalacheckOps(scalatestVersion: String): ModuleID = {
    val suffix = scalatestVersion match {
      case `scalatest2Version` => "_1-12"
      case `scalatest3Version` => "_1-13"
    }
    "com.rallyhealth" %% s"scalacheck-ops$suffix" % scalacheckOpsVersion
  }

  def scalatest(scalatestVersion: String): ModuleID = {
    "org.scalatest" %% "scalatest" % scalatestVersion
  }

}

package play.api.libs.json.ops

import org.scalatest.FlatSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import play.api.libs.json._
import play.api.libs.json.scalacheck.JsValueGenerators

import scala.annotation.tailrec
import scala.util.Random

class JsonTransformSpec extends FlatSpec
with GeneratorDrivenPropertyChecks
with JsValueGenerators
with JsonImplicits {

  @tailrec private def verifyAllRedacted(all: Seq[(JsPath, JsValue)]): Unit = {
    val invalid = all collect {
      case (path, value) if value != JsonTransform.RedactedValue => path
    }
    assert(invalid.isEmpty, s"The following paths are invalid: ${invalid.mkString(", ")}")
    val nextGen = all flatMap {
      case (path, JsArray(items)) => items.zipWithIndex map {
        case (item, i) => (JsPath(path.path :+ IdxPathNode(i)), item)
      }
      case (path, JsObject(fields)) => fields map {
        case (k, v) => (path \ k, v)
      }
      case _ => Nil
    }
    if (nextGen.nonEmpty) {
      verifyAllRedacted(nextGen)
    }
  }

  "redactPaths" should "redact selected fields by path at the top level" in {
    forAll { (obj: JsObject) =>
      val topLevelPaths: Seq[JsPath] = obj.fields.map(__ \ _._1)
      whenever(topLevelPaths.nonEmpty) {
        val redactedPaths: Seq[JsPath] = Random.shuffle(topLevelPaths) take Random.nextInt(topLevelPaths.size)
        implicit val redactor = JsonTransform.redactPaths[Any](redactedPaths)
        val redacted = obj.transformAs[Any]
        // Useful for debugging
//        if (redactedPaths.nonEmpty) {
//          println(Json.prettyPrint(obj))
//          println(s"with redacted paths (${redactedPaths.mkString(", ")}):")
//          println(Json.prettyPrint(redacted))
//        }
        for (path <- redactedPaths) {
          assertResult(JsonTransform.RedactedValue) {
            path.asSingleJson(redacted)
          }
        }
      }
    }
  }

  "redactAll" should "redact all fields of all paths" in {
    implicit val redactor = JsonTransform.redactAll[Any]()
    forAll { (obj: JsObject) =>
      val redacted = obj.transformAs[Any]
      verifyAllRedacted(Seq(__ -> redacted))
    }
  }
}

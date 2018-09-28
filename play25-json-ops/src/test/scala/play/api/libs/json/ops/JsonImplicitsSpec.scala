package play.api.libs.json.ops

import org.scalatest.FreeSpec
import play.api.libs.json.{Format, Json}
import org.scalatest.prop.GeneratorDrivenPropertyChecks._

case class KeyWrapper(key: String)

class JsonImplicitsSpec extends FreeSpec {

  private val exampleJson = Json.obj(
    "A" -> "value",
    "B" -> "other"
  )
  private val exampleMap = Map(
    KeyWrapper("A") -> "value",
    KeyWrapper("B") -> "other"
  )
  private val exampleMapFormat = {
    formatMap[KeyWrapper, String](Format.of[String], KeyWrapper, _.key)
  }

  "implicit function resolution of formatMap should throw an error on writes" in {
    implicit val fromString: String => KeyWrapper = KeyWrapper
    assertDoesNotCompile {
      """
      Json.toJson(exampleMap)
      """
    }
  }

  "implicit function resolution of formatMap should throw an error on reads" in {
    implicit val fromString: String => KeyWrapper = KeyWrapper
    assertDoesNotCompile {
      """
      Json.fromJson[Map[KeyWrapper, String]](exampleJson)
      """
    }
  }

  "implicit conversion resolution of formatMap should throw an error on writes" in {
    import scala.language.implicitConversions
    implicit def fromString(key: String): KeyWrapper = KeyWrapper(key)
    assertDoesNotCompile {
      """
      Json.toJson(exampleMap)
      """
    }
  }

  "implicit conversion resolution of formatMap should throw an error on reads" in {
    import scala.language.implicitConversions
    implicit def fromString(key: String): KeyWrapper = KeyWrapper(key)
    assertDoesNotCompile {
      """
      Json.fromJson[Map[KeyWrapper, String]](exampleJson)
      """
    }
  }

  "explicit call to write should format the Json correctly" in {
    assertResult(exampleJson) {
      exampleMapFormat.writes(exampleMap)
    }
  }

  "explicit call to read should read correctly formatted Json" in {
    assertResult(exampleMap) {
      exampleMapFormat.reads(exampleJson).recoverTotal { err =>
        throw InvalidJsonException[Map[KeyWrapper, String]](exampleJson, err)
      }
    }
  }

  "formatter should read every value it writes and write it out the same way" in {
    forAll { value: Map[String, String] =>
      val keyWrappedMap = value.map {
        case (k, v) => (KeyWrapper(k), v)
      }
      val json = exampleMapFormat.writes(keyWrappedMap)
      val parsedMap = exampleMapFormat.reads(json).recoverTotal { err =>
        throw InvalidJsonException[Map[KeyWrapper, String]](json, err)
      }
      assertResult(keyWrappedMap)(parsedMap)
    }
  }
}

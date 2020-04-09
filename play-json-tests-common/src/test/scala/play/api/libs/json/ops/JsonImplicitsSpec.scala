package play.api.libs.json.ops

import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalacheck.ops._
import org.scalatest.FreeSpec
import play.api.libs.json.{Format, Json}
import org.scalatest.prop.GeneratorDrivenPropertyChecks._

case class KeyWrapper(key: String)
object KeyWrapper {
  implicit val arbKeyWrapper: Arbitrary[KeyWrapper] = Arbitrary(Gen.string.map(KeyWrapper(_)))
  implicit val shrinkKeyWrapper: Shrink[KeyWrapper] = Shrink { k =>
    Shrink.shrinkString.shrink(k.key).map(KeyWrapper(_))
  }

  implicit lazy val format: Format[KeyWrapper] = Format.asString(convertFromString, _.key)
  implicit val writeKey: WritesKey[KeyWrapper] = WritesKey(_.key)
  implicit val readKey: ReadsKey[KeyWrapper] = ReadsKey.of[String].map(KeyWrapper(_))
  implicit lazy val convertFromString: String => KeyWrapper = KeyWrapper(_)
}

class JsonImplicitsSpec extends FreeSpec {

  private val exampleJson = Json.obj(
    "A" -> "value",
    "B" -> "other"
  )

  private val exampleMap = Map(
    KeyWrapper("A") -> "value",
    KeyWrapper("B") -> "other"
  )

  private val exampleMapFormat = Format.of[Map[KeyWrapper, String]]

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

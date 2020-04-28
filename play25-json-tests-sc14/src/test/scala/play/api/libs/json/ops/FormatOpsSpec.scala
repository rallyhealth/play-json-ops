package play.api.libs.json.ops

import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class FormatOpsSpec extends AnyWordSpec {

  "Format.empty[Iterable[Nothing]]" should {
    val formatEmpty = Format.of[Iterable[Nothing]]

    "reads Seq.empty" in {
      assertResult(JsSuccess(Seq.empty)) {
        formatEmpty.reads(Json.arr())
      }
    }

    "writes Seq.empty" in {
      assertResult(Json.arr()) {
        formatEmpty.writes(Seq.empty)
      }
    }

    "invalidate a non-empty array" in {
      val result = formatEmpty.reads(Json.arr(1))
      assert(result.isError)
    }

    "not compile when writing a non-empty seq" in {
      assertDoesNotCompile("formatEmpty.writes(Seq(1))")
    }
  }

  "Format.empty[List[Nothing]]" should {
    val formatEmptyList = Format.of[List[Nothing]]

    "reads Nil" in {
      assertResult(JsSuccess(Nil)) {
        formatEmptyList.reads(Json.arr())
      }
    }

    "writes Nil" in {
      assertResult(Json.arr()) {
        formatEmptyList.writes(Nil)
      }
    }

    "not compile when writing an empty seq" in {
      assertDoesNotCompile {
        """formatEmptyList.writes(Seq())"""
      }
    }
  }

  "Format.enumValueString" should {
    val formatEnumString = Format.enumValueString(EnumExample)

    "reads a valid value" in {
      assertResult(JsSuccess(EnumExample.A)) {
        formatEnumString.reads(JsString(EnumExample.A.toString))
      }
    }

    "fails to read an invalid value" in {
      assertResult(JsError("error.expected.enumexample: No value found for 'ERROR'")) {
        formatEnumString.reads(JsString("ERROR"))
      }
    }

    "writes am enum value" in {
      assertResult(JsString(EnumExample.A.toString)) {
        formatEnumString.writes(EnumExample.A)
      }
    }

    "not write a string" in {
      assertDoesNotCompile {
        """formatEnumString.writes("A")"""
      }
    }
  }

  "Format.pure" should {

    "read the expected object" in {
      assertResult(PureObjectExample) {
        PureObjectExample.format.reads(JsBoolean(false)).get // any value will read PureExample
      }
    }

    "write the expected json" in {
      assertResult(PureObjectExample.alwaysWritenAs) {
        PureObjectExample.format.writes(PureObjectExample)
      }
    }
  }

  "OFormat.pure" should {

    "read the expected object" in {
      assertResult(PureObjectExample) {
        PureObjectExample.oformat.reads(JsBoolean(false)).get // any value will read PureExample
      }
    }

    "write the expected json" in {
      assertResult(PureObjectExample.alwaysWritenAsObject) {
        PureObjectExample.oformat.writes(PureObjectExample)
      }
    }
  }

}

object PureObjectExample {

  val alwaysWritenAs: JsValue = JsString("pure")
  val alwaysWritenAsObject: JsObject = Json.obj("value" -> alwaysWritenAs)

  val format: Format[PureObjectExample.type] = Format.pure(this, alwaysWritenAs)
  val oformat: OFormat[PureObjectExample.type] = OFormat.pure(this, alwaysWritenAsObject)
}

object EnumExample extends Enumeration {
  val A, B, C = Value
}

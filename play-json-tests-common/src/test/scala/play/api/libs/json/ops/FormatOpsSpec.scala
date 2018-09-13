package play.api.libs.json.ops

import org.scalatest.WordSpec
import play.api.libs.json._

class FormatOpsSpec extends WordSpec {

  "Format.empty(List)" should {
    val formatList = FormatOps.empty(Nil)

    "reads Nil" in {
      assertResult(JsSuccess(Nil)) {
        formatList.reads(Json.arr())
      }
    }

    "writes Nil" in {
      assertResult(Json.arr()) {
        formatList.writes(Nil)
      }
    }

    "writes List[Nothing]" in {
      assertResult(Json.arr()) {
        formatList.writes(List.empty[Nothing])
      }
    }

    "invalidate a non-empty array" in {
      val result = formatList.reads(Json.arr(1))
      assert(result.isError)
    }

    "not compile when writing a seq" in {
      assertDoesNotCompile("formatList.writes(Seq())")
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

package play.api.libs.json.ops

import org.scalatest.WordSpec
import play.api.libs.json._

class FormatOpsSpec extends WordSpec {

  "Format.pure" should {

    "read the expected object" in {
      assertResult(PureObjectExample) {
        JsBoolean(false).as[PureObjectExample.type] // any value will read PureExample
      }
    }

    "write the expected json" in {
      assertResult(PureObjectExample.alwaysWritenAs) {
        Json.toJson(PureObjectExample)
      }
    }
  }

}

object PureExample {

  val alwaysWritenAs: JsValue = JsNull

  implicit val format: Format[PureObjectExample.type] = Format.pure(PureObjectExample, alwaysWritenAs)
}

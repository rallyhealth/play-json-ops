package play.api.libs.json.ops

import org.scalatest.WordSpec
import play.api.libs.json._

class OFormatOpsSpec extends WordSpec {

  "OFormat.pure" should {

    "read the expected object" in {
      assertResult(PureObjectExample) {
        JsBoolean(false).as[PureObjectExample.type] // any value will read PureExample
      }
    }

    "write the expected json object" in {
      assertResult(PureObjectExample.alwaysWritenAs) {
        Json.toJson(PureObjectExample)
      }
    }
  }

}

object PureObjectExample {

  val alwaysWritenAs: JsObject = Json.obj("value" -> "example")

  implicit val format: OFormat[PureObjectExample.type] = OFormat.pure(PureObjectExample, alwaysWritenAs)
}

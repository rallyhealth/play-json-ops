package play.api.libs.json.ops

import org.scalatest.WordSpec
import play.api.libs.json._

class OFormatOpsSpec extends WordSpec {

  "OFormat.pure" should {

    "read the expected object" in {
      val example = JsBoolean(false).as[PureObjectExample.type] // any value will read PureExample
      assert(example === PureObjectExample)
    }

    "write the expected json object" in {
      val json = Json.toJson(PureObjectExample)
      assert(json === PureObjectExample.alwaysWritenAs)
    }
  }

}

object PureObjectExample {

  val alwaysWritenAs: JsObject = Json.obj("value" -> "example")

  implicit val format: OFormat[PureObjectExample.type] = OFormat.pure(PureObjectExample, alwaysWritenAs)
}

package play.api.libs.json.ops

import org.scalatest.WordSpec
import play.api.libs.json._

class FormatOpsSpec extends WordSpec {

  "Format.pure" should {

    "read the expected object" in {
      val example = JsBoolean(false).as[PureObjectExample.type] // any value will read PureExample
      assert(example === PureObjectExample)
    }

    "write the expected json" in {
      val json = Json.toJson(PureObjectExample)
      assert(json === PureObjectExample.alwaysWritenAs)
    }
  }

}

object PureExample {

  val alwaysWritenAs: JsValue = JsNull

  implicit val format: Format[PureObjectExample.type] = Format.pure(PureObjectExample, alwaysWritenAs)
}

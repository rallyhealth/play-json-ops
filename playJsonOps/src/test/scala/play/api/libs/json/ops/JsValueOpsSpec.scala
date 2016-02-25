package play.api.libs.json.ops

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json._
import play.api.libs.json.scalacheck.JsValueGenerators

case class Troll(value: String)

object Troll {
  implicit val format = Json.format[Troll]
}

class JsValueOpsSpec extends FlatSpec
with GeneratorDrivenPropertyChecks
with Matchers
with JsValueGenerators
with JsonImplicits {

  implicit val arbTroll = Arbitrary(Gen.identifier.map(Troll(_)))

  "transformAs" should "use the implicit JsonTransform" in {
    val troll = JsString("trolled :)")
    implicit val transform = JsonTransform[Troll](_ => troll)
    forAll() { (json: JsValue) =>
      assertResult(troll) {
        json.transformAs[Troll]
      }
    }
  }

  behavior of "asOrThrow"

  it should "convert the json as normal" in {
    implicit val transform = JsonTransform.redactPaths[Troll](Seq(__ \ "value"))
    forAll() { (troll: Troll) =>
      assertResult(troll) {
        Json.toJson(troll).asOrThrow[Troll]
      }
    }
  }

  it should "transform the json when throwing an exception" in {
    implicit val transform = JsonTransform.redactPaths[Troll](Seq(__ \ "value"))
    forAll() { (json: JsObject) =>
      val ex = intercept[InvalidJsonException] {
        json.asOrThrow[Troll]
      }
      assertResult(JsonTransform.RedactedValue) {
        ex.json \ "value"
      }
    }
  }
}

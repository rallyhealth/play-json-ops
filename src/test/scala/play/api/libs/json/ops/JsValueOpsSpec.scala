package play.api.libs.json.ops

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Matchers, FlatSpec, ParallelTestExecution}
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
      assert(json.transformAs[Troll] == troll)
    }
  }

  behavior of "asOrThrow"

  it should "convert the json as normal" in {
    implicit val transform = JsonTransform.redactPaths[Troll](Seq(__ \ "value"))
    forAll() { (troll: Troll) =>
      val parsed = Json.toJson(troll).asOrThrow[Troll]
      assert(parsed == troll)
    }
  }

  it should "transform the json when throwing an exception" in {
    implicit val transform = JsonTransform.redactPaths[Troll](Seq(__ \ "value"))
    forAll() { (json: JsObject) =>
      val ex = intercept[InvalidJson] {
        json.asOrThrow[Troll]
      }
      assert(ex.json \ "value" == JsonTransform.RedactedValue)
    }
  }
}

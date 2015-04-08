package play.api.libs.json.ops

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json._

class FormatAbstractOpsSpec extends FlatSpec
  with GeneratorDrivenPropertyChecks
  with JsonImplicits
  with Matchers {

  implicit val arbA: Arbitrary[SpecificA] = Arbitrary {
    for {
      value <- Arbitrary.arbitrary[String]
    } yield SpecificA(value)
  }

  implicit val arbB: Arbitrary[SpecificB] = Arbitrary {
    for {
      value <- Arbitrary.arbitrary[String]
    } yield SpecificB(value)
  }

  implicit val arbGeneric: Arbitrary[Generic] = Arbitrary {
    Gen.oneOf(arbA.arbitrary, arbB.arbitrary)
  }

  behavior of "formatWithType"

  it should "write the specific type of json along with the type key" in {
    forAll { (o: Generic) =>
      val json = Json.toJson(o)
      val expectedValue = o match {
        case SpecificA(v) => v
        case SpecificB(v) => v
      }
      val expected = Json.obj(Generic.keyFieldName -> o.key, "value" -> expectedValue)
      assert(json == expected)
    }
  }

  it should "read what it writes" in {
    forAll { (o: Generic) =>
      val json = Json.toJson(o)
      val parsed = json.as[Generic]
      assert(o == parsed)
    }
  }

  it should "fail with an UnrecognizedTypeKey exception when the key is unmatched" in {
    implicit val format: OFormat[Generic] = Json.formatAbstract[Generic] {
      // case SpecificA.key => OFormat.of[SpecificA]  // oops, forgot to add this
      case SpecificB.key => OFormat.of[SpecificB]
    }
    val a = arbA.arbitrary.sample.get
    an[UnrecognizedTypeKey] shouldBe thrownBy { Json.toJson(a) }
    val b = arbB.arbitrary.sample.get
    Json.toJson(b)  // should be fine
  }
}

sealed trait Generic {
  def key: String
}

object Generic extends JsonImplicits {

  val keyFieldName = "key"

  implicit val extractor: TypeKeyExtractor[Generic] =
    Json.extractTypeKey[Generic].using(_.key, __ \ keyFieldName)

  implicit val format: OFormat[Generic] = Json.formatAbstract[Generic] {
    case SpecificA.key => OFormat.of[SpecificA]
    case SpecificB.key => OFormat.of[SpecificB]
  }
}

case class SpecificA(value: String) extends Generic {

  override def key: String = SpecificA.key
}

object SpecificA extends JsonImplicits {
  val key = "A"

  implicit val format: OFormat[SpecificA] = Json.formatWithType[SpecificA, Generic](Json.oformat[SpecificA])
}

case class SpecificB(value: String) extends Generic {

  override def key: String = SpecificB.key
}

object SpecificB extends JsonImplicits {
  val key = "B"

  implicit val format: OFormat[SpecificB] = Json.formatWithType[SpecificB, Generic](Json.oformat[SpecificB])
}
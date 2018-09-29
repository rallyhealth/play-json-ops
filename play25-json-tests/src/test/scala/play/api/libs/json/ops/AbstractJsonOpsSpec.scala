package play.api.libs.json.ops

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json._

class AbstractJsonOpsSpec extends WordSpec
  with GeneratorDrivenPropertyChecks
  with JsonImplicits
  with ExampleGenerators
  with Matchers {

  "AbstractJsonOps.formatWithTypeKeyOf" should {

    "write the the type key, along with the value, if present" in {
      forAll { (o: SingleField) =>
        val json = Json.toJson(o)(Generic.format)
        val expectedKey = Json.obj(Generic.keyFieldName -> o.key)
        // exclude the invisible values
        val expectedValue = if (o.hidesValueFromJson) Json.obj() else Json.obj("value" -> o.value)
        val expected = expectedKey ++ expectedValue
        assertResult(expected)(json)
      }
    }
  }

  "AbstractJsonOps.formatAbstract" should {

    "read what it writes" in {
      forAll { (o: Generic) =>
        val json = Json.toJson(o)
        val parsed = json.as[Generic]
        assertResult(o, "the deserialized value did not equal the original pre-serialized value")(parsed)
      }
    }

    "fail with an UnrecognizedTypeKeyException exception when the key is unmatched" in {
      val brokenFormat: OFormat[Generic] = Json.formatAbstract[Generic] {
        case SpecificFieldA.key => OFormat.of[SpecificFieldA]
        // oops, forgot to add format for B
      }
      val a = genA.sample.get
      Json.toJson(a)(brokenFormat)  // should be fine
      val b = genB.sample.get
      an[UnrecognizedTypeKeyException] shouldBe thrownBy { Json.toJson(b)(brokenFormat) }
    }

    "fail with an WrongAbstractFormatException exception when the wrong OFormat used" in {
      val brokenFormat: OFormat[Generic] = Json.formatAbstract[Generic] {
        case SpecificFieldA.key => OFormat.of[SpecificFieldA]
        case SpecificFieldB.key => OFormat.of[SpecificFieldA]  // oops, this should be B
      }
      val a = genA.sample.get
      Json.toJson(a)(Generic.format)  // should be fine
      val b = genB.sample.get
      an[WrongAbstractFormatException] shouldBe thrownBy { Json.toJson(b)(brokenFormat) }
    }
  }

  "AbstractJsonOps.usingKeyObject" should {

    "allow custom logic to occur in the pattern matching type key" in {
      val keyPath = __ \ "even"
      val evenJson = keyPath.write[Boolean].writes(true)
      val oddJson = keyPath.write[Boolean].writes(false)
      val evenValue = 2
      val oddValue = 3
      val extractor = TypeKeyExtractor.extractTypeKey[Int].usingKeyObject(keyPath -> Reads.of[Boolean]) {
        case even if even % 2 == 0 => evenJson
        case odd => oddJson
      }
      val fancyFormat: OFormat[Int] = Json.formatAbstract[Int] {
        case true => OFormat.pure(evenValue, evenJson)
        case false => OFormat.pure(oddValue, oddJson)
      } (extractor)
      for (n <- 0 to 10) {
        def isEven = n % 2 == 0
        val nJson = Json.toJson(n)(fancyFormat)
        assertResult(if (isEven) evenJson else oddJson) {
          nJson
        }
        val backToInt = Json.fromJson[Int](nJson)(fancyFormat)
        assertResult(if (isEven) JsSuccess(evenValue, keyPath) else JsSuccess(oddValue, keyPath)) {
          backToInt
        }
      }
    }

    "fail with an JsonTypeKeyReadException when the extractor cannot distinguish which is the unique key" in {
      val duplicateValue = "duplicate"
      val duplicateJson = Json.obj(
        "key1" -> duplicateValue,
        "key2" -> duplicateValue
      )
      val extractor = TypeKeyExtractor.extractTypeKey[String].usingKeyObject(
        __ \ "key1" -> Reads.of[String],
        __ \ "key2" -> Reads.of[String]
      ) {
        case `duplicateValue` => duplicateJson
      }
      a [JsonTypeKeyReadException] shouldBe thrownBy {
        extractor.readKeyFromModel(duplicateValue)
      }
    }
  }
}

trait ExampleGenerators {

  def genA: Gen[SpecificFieldA] = Gen.identifier.map(SpecificFieldA(_))

  def genB: Gen[SpecificFieldB] = Gen.identifier.map(SpecificFieldB(_))

  def genC: Gen[SpecificObjectC.type] = Gen.const(SpecificObjectC)

  def genD: Gen[SpecificObjectD.type] = Gen.const(SpecificObjectD)

  def genE: Gen[ComplexStringKey] = {
    for {
      id <- Gen.identifier
      value <- Gen.alphaStr
      otherString <- Gen.alphaStr
    } yield ComplexStringKey(id, value, otherString)
  }

  def genF: Gen[ComplexBooleanKey] = {
    for {
      id <- Gen.identifier
      value <- Gen.alphaStr
      otherBoolean <- Gen.oneOf(true, false)
    } yield ComplexBooleanKey(id, value, otherBoolean)
  }

  def genSpecific: Gen[SingleField] = Gen.oneOf(genA, genB, genC, genD)

  def genComplex: Gen[ComplexKey] = Gen.oneOf(genE, genF)

  implicit val arbSpecific: Arbitrary[SingleField] = Arbitrary(genSpecific)

  implicit val arbGeneric: Arbitrary[Generic] = Arbitrary {
    Gen.oneOf(genSpecific, genComplex)
  }
}

sealed trait Generic {
  def key: String
  def value: String
}

object Generic extends JsonImplicits {

  val keyFieldName = "key"

  implicit val extractor: TypeKeyExtractor[Generic] =
    Json.extractTypeKey[Generic].usingKeyField(_.key, __ \ keyFieldName)

  implicit val format: OFormat[Generic] = Json.formatAbstract[Generic] {
    case SpecificFieldA.key => OFormat.of[SpecificFieldA]
    case SpecificFieldB.key => OFormat.of[SpecificFieldB]
    case SpecificObjectC.key => OFormat.of[SpecificObjectC.type]
    case SpecificObjectD.key => OFormat.of[SpecificObjectD.type]
    case ComplexKey.key => OFormat.of[ComplexKey]
  }
}

sealed trait SingleField extends Generic {
  def hidesValueFromJson: Boolean
}

case class SpecificFieldA(value: String) extends SingleField {
  override def key: String = SpecificFieldA.key
  override def hidesValueFromJson: Boolean = false
}
object SpecificFieldA extends JsonImplicits {
  final val key = "A"
  implicit val format: OFormat[SpecificFieldA] = Json.formatWithTypeKeyOf[Generic].addedTo(Json.format[SpecificFieldA])
}

case class SpecificFieldB(value: String) extends SingleField {
  override def key: String = SpecificFieldB.key
  override def hidesValueFromJson: Boolean = false
}
object SpecificFieldB extends JsonImplicits {
  final val key = "B"
  implicit val format: OFormat[SpecificFieldB] = Json.formatWithTypeKeyOf[Generic].addedTo(Json.format[SpecificFieldB])
}

case object SpecificObjectC extends SingleField {
  override final val key: String = "C"
  override def value: String = "invisible constant C"
  override def hidesValueFromJson: Boolean = true
  implicit val format: OFormat[this.type] = Json.formatWithTypeKeyOf[Generic].pure(SpecificObjectC)
}

case object SpecificObjectD extends SingleField {
  override final val key: String = "D"
  def value: String = "visible constant D"
  override def hidesValueFromJson: Boolean = false
  implicit val format: OFormat[SpecificObjectD.type] =
    Json.formatWithTypeKeyOf[Generic].pure(SpecificObjectD, Json.obj("value" -> value))
}

sealed trait ComplexKey extends Generic {
  def id: String
  override def key = ComplexKey.key
}
object ComplexKey {

  final val key = "Complex"

  implicit val extractor: TypeKeyExtractor[ComplexKey] = Json.extractTypeKey[ComplexKey].usingKeyObject(
    (__ \ "otherBoolean") -> Format.of[Boolean],
    (__ \ "otherString")  -> Format.of[String]
  ) {
    case f: ComplexBooleanKey => Json.obj("otherBoolean" -> f.otherBoolean)
    case e: ComplexStringKey  => Json.obj("otherString" -> e.otherString)
  }

  implicit val format: OFormat[ComplexKey] = Json.formatAbstract[ComplexKey] {
    case otherString: String   => OFormat.of[ComplexStringKey]
    case otherBoolean: Boolean => OFormat.of[ComplexBooleanKey]
  }
}

case class ComplexStringKey(id: String, value: String, otherString: String) extends ComplexKey
object ComplexStringKey {
  implicit val format: OFormat[ComplexStringKey] = Json.formatWithTypeKeyOf[ComplexKey].addedTo(Json.format[ComplexStringKey])
}

case class ComplexBooleanKey(id: String, value: String, otherBoolean: Boolean) extends ComplexKey
object ComplexBooleanKey {
  implicit val format: OFormat[ComplexBooleanKey] = Json.formatWithTypeKeyOf[ComplexKey].addedTo(Json.format[ComplexBooleanKey])
}

package play.api.libs.json.scalacheck

import org.scalacheck.{Gen, Prop}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Assertions, FlatSpec, Matchers}
import play.api.libs.json._

import scala.annotation.tailrec

class JsValueGeneratorsSpec extends FlatSpec
with GeneratorDrivenPropertyChecks
with Matchers
with JsValueGenerators {

  // Controls the overall time these tests take to run
  // For better testing coverage, this needs to be greater than defaultMaxDepth / defaultMaxWidth
  implicit val maxDepth: Depth = defaultMaxDepth + 3
  implicit val maxWidth: Width = defaultMaxWidth + 3

  @tailrec private def maxPathDepth(all: Seq[JsValue], current: Depth = 0): Depth = {
    val nestedValues = all flatMap {
      case JsArray(values) => values
      case JsObject(fields) => fields.map(_._2)
      case primitive: JsValue => Nil
    }
    if (nestedValues.isEmpty) current
    else maxPathDepth(nestedValues, current + 1)
  }

  "Depth" should behave like aCount(Depth)

  "Width" should behave like aCount(Width)

  /* Be careful when defining implicits in an outer class, sometimes the compiler will
   * not throw an "ambiguous implicit values" exception when the values extend AnyVal.
   *
   * Instead of getting a duplicate implicit compiler error, the tests were running with
   * super.maxDepth and super.maxWidth, so I figured I would sanity check here.
   *
   * If you see a compiler error or this test fails, then you must have made something
   * implicit that maybe shouldn't be, but at least if this test fails then you know
   * why you are seeing a StackOverflow exception.
   *
   * This is primarily just to sanity check that the behavior of implicit Depth and Width
   * work as intended.
   */
  "Depth and Width" should "be differentiated in implicit parameters by type at compile-time" in {
    val localScope = new JsValueGeneratorsSubclass
    localScope.verifyImplicitOverrides()
  }

  "genJsPrimitive" should behave like aPrimitiveJsValue(genJsPrimitive())
  it should behave like itHasSymmetricJsonSerialization(genJsPrimitive(includeUndefined = false))

  "genJsValue with a depth of 0" should behave like aPrimitiveJsValue(genJsValue(Depth(0)))

  "genJsValue" should behave like aJsValueContainer(genJsValue(_, _))
  it should behave like itHasSymmetricJsonSerialization(genJsValue())

  "genJsArray" should behave like aJsValueContainer(genJsArray(_, _))
  it should behave like itHasSymmetricJsonSerialization(genJsArray())

  "genJsObject" should behave like aJsValueContainer(genJsObject(_, _))
  it should behave like itHasSymmetricJsonSerialization(genJsObject())

  "genSafeBigDecimal" should "generate BigDecimals greater than Double.MaxValue " in {
    val biggerExists = Prop.exists((big: BigDecimal) => big > Double.MaxValue)
    val biggerThanDouble = biggerExists(Gen.Parameters.default)
    assert(!biggerThanDouble.failure)
  }

  it should "generate BigDecimals smaller than Double.MinValue" in {
    val smallerExists = Prop.exists((big: BigDecimal) => big < Double.MinValue)
    val smallerThanDouble = smallerExists(Gen.Parameters.default)
    assert(!smallerThanDouble.failure)
  }

  def aCount(count: Int => Counted): Unit = {
    it should "throw an exception on negative numbers" in {
      forAll(Gen.negNum[Int]) { n =>
        an[IllegalArgumentException] shouldBe thrownBy { count(n) }
      }
    }

    it should "be identical to zero or positive" in {
      forAll(Gen.posNum[Int]) { n =>
        assertResult(n) {
          count(n).count
        }
      }
    }
  }

  def aPrimitiveJsValue(genJs: Gen[_ <: JsValue]): Unit = {
    it should "only contain primitive (non-nested) values" in {
      forAll(genJs) { json =>
        assertResult(0) {
          maxPathDepth(Seq(json)).count
        }
      }
    }
  }

  def aJsValueContainer(genJs: (Depth, Width) => Gen[_ <: JsValue]): Unit = {
    it should "should reach a depth greater than 1" in {
      Prop.exists(genJs(Depth(1), maxWidth)) { json =>
        maxPathDepth(Seq(json)) > 1
      }
    }

    it should "have a depth no larger than provided" in {
      val genJsWithDepth: Gen[(Int, JsValue)] = for {
        maxDepth <- Gen.choose(1, maxDepth.depth)
        js <- genJs(Depth(maxDepth), maxWidth)
      } yield (maxDepth, js)
      forAll(genJsWithDepth) { case (max, json) =>
        assert(maxPathDepth(Seq(json)) <= max)
      }
    }
  }

  def anArbitraryJsValue(genJs: Gen[_ <: JsValue]): Unit = {
    it should "never generate more depth than the specified depth" in {
      forAll() { (json: JsValue) =>
        assert(maxPathDepth(Seq(json)) <= maxDepth)
      }
    }
  }

  def itHasSymmetricJsonSerialization(genJs: Gen[_ <: JsValue]): Unit = {
    it should "always generate values that can be serialized and deserialized to and from a string" in {
      forAll() { (json: JsValue) =>
        assertResult(json) {
          val serialized = Json.stringify(json)
          val deserialized = Json.parse(serialized)
          deserialized
        }
      }
    }
  }
}

class JsValueGeneratorsSubclass(implicit localDepth: Depth, localWidth: Width)
  extends JsValueGenerators
  with Assertions
  with TypeCheckedTripleEquals {

  def doVerifyImplicitOverrides(implicit depth: Depth, width: Width): Unit = {
    assume(depth === localDepth)
    assume(width === localWidth)
  }

  def verifyImplicitOverrides(): Unit = doVerifyImplicitOverrides
}

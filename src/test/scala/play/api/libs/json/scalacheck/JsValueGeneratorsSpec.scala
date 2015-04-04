package play.api.libs.json.scalacheck

import org.scalacheck.{Prop, Gen, Arbitrary}
import org.scalatest.{Matchers, ParallelTestExecution, FlatSpec}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import play.api.libs.json._

import scala.annotation.tailrec

class JsValueGeneratorsSpec extends FlatSpec with ParallelTestExecution
with GeneratorDrivenPropertyChecks
with Matchers
with JsValueGenerators {

  // removes implicits in JsValueGenerators to avoid duplicate implicits in tests
  override def arbJsValue: Arbitrary[JsValue] = super.arbJsValue
  override def arbJsObject: Arbitrary[JsObject] = super.arbJsObject
  override def arbJsArray: Arbitrary[JsArray] = super.arbJsArray

  // Controls the overall time these tests take to run
  override val defaultMaxDepth = Depth(4)
  override val defaultMaxWidth = Width(4)

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

  /* I saw a strange issue when testMaxDepth / testMaxWidth is implicit
   * and Depth / Width extend AnyVal.
   *
   * Instead of getting a duplicate implicit compiler error, the tests were running with
   * super.maxDepth and super.maxWidth, so I figured I would sanity check here.
   * If you see a compiler error or this test fails, then you must have made something
   * implicit that maybe shouldn't be, but at least if this test fails then you know
   * why you are seeing a StackOverflow exception.
   */
  "Depth and Width" should "be differentiated in implicit parameters by type at compile-time" in {
    implicit val localDepth = Depth(11)
    implicit val localWidth = Width(22)
    def verifyNotEqual(implicit depth: Depth, width: Width): Unit = {
      assert(depth == localDepth)
      assert(width == localWidth)
    }
    verifyNotEqual
  }

  behavior of "genJsPrimitive"

  it should behave like aPrimitiveJsValue(genJsPrimitive)

  "genJsValue with a depth of 0" should behave like aPrimitiveJsValue(genJsValue(Depth(0)))

  "genJsValue" should behave like aJsValueContainer(genJsValue(_, _))

  "genJsArray" should behave like aJsValueContainer(genJsArray(_, _))

  "genJsObject" should behave like aJsValueContainer(genJsObject(_, _))

  def aCount(count: Int => Counted): Unit = {
    it should "throw an exception on negative numbers" in {
      forAll(Gen.negNum[Int]) { n =>
        an[IllegalArgumentException] shouldBe thrownBy { count(n) }
      }
    }

    it should "be identical to zero or positive" in {
      forAll(Gen.posNum[Int]) { n =>
        assert(count(n) === n)
      }
    }
  }

  def aPrimitiveJsValue(genJs: Gen[_ <: JsValue]): Unit = {
    it should "only contain primitive (non-nested) values" in {
      forAll(genJs) { json =>
        assert(maxPathDepth(Seq(json)) === 0)
      }
    }
  }

  def aJsValueContainer(genJs: (Depth, Width) => Gen[_ <: JsValue]): Unit = {
    it should "should reach a depth greater than 1" in {
      Prop.exists(genJs(Depth(1), defaultMaxWidth)) { json =>
        maxPathDepth(Seq(json)) > 1
      }
    }

    it should "have a depth no larger than provided" in {
      val genJsWithDepth: Gen[(Int, JsValue)] = for {
        maxDepth <- Gen.choose(1, defaultMaxDepth.depth)
        js <- genJs(Depth(maxDepth), defaultMaxWidth)
      } yield (maxDepth, js)
      forAll(genJsWithDepth) { case (max, json) =>
        maxPathDepth(Seq(json)) <= max
      }
    }
  }

  def anArbitraryJsValue(genJs: Gen[_ <: JsValue]): Unit = {
    it should "never generate more depth than the specified depth" in {
      forAll(genJs) { json =>
        println(Json.prettyPrint(json))
        assert(maxPathDepth(Seq(json)) <= defaultMaxDepth)
      }
    }
  }
}

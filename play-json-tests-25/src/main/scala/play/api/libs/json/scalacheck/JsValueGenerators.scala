package play.api.libs.json.scalacheck

import java.math.MathContext

import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck.Shrink._
import org.scalacheck.{Arbitrary, Gen, Shrink}
import play.api.libs.json._

import scala.language.implicitConversions

trait JsValueGenerators {

  /**
   * The maximum number of fields of a [[JsObject]] or elements of a [[JsArray]] to construct when
   * generating one of these nested [[JsValue]]s.
   */
  def defaultMaxWidth: Width = Width(2)

  /**
   * The maximum number of levels deep where nested values ([[JsObject]]s or [[JsArray]]s) can be generated.
   *
   * In other words:
   * - A depth of 0 generates only primitive [[JsValue]]s
   * - A depth of 1 generates any type of [[JsValue]] where all nested values contain only primitive [[JsValue]]s.
   * - A depth of n generates any type of [[JsValue]] where all nested values contain [[JsValue]]s with a depth of n - 1
   */
  def defaultMaxDepth: Depth = Depth(2)

  implicit def arbJsValue(implicit
    maxDepth: Depth = defaultMaxDepth,
    maxWidth: Width = defaultMaxWidth): Arbitrary[JsValue] = Arbitrary(genJsValue)

  implicit def arbJsObject(implicit
    maxDepth: Depth = defaultMaxDepth,
    maxWidth: Width = defaultMaxWidth): Arbitrary[JsObject] = Arbitrary(genJsObject)

  implicit def arbJsArray(implicit
    maxDepth: Depth = defaultMaxDepth,
    maxWidth: Width = defaultMaxWidth): Arbitrary[JsArray] = Arbitrary(genJsArray)

  implicit def arbJsString(implicit arbString: Arbitrary[String]): Arbitrary[JsString] = Arbitrary {
    arbString.arbitrary map JsString
  }

  implicit def arbJsNumber: Arbitrary[JsNumber] = Arbitrary {
    genSafeBigDecimal map JsNumber
  }

  implicit def arbJsBoolean: Arbitrary[JsBoolean] = Arbitrary {
    Gen.oneOf(true, false) map JsBoolean
  }

  /**
   * The Jaxson parser has trouble with very large exponents of BigDecimals.
   *
   * I couldn't quite pin the problem down to precision, scale, or both, so
   */
  def genSafeBigDecimal: Gen[BigDecimal] = {
    def chooseBigInt: Gen[BigInt] =
      sized((s: Int) => choose(-s, s)) map (x => BigInt(x))
    def genBigInt: Gen[BigInt] = Gen.frequency(
      (10, chooseBigInt),
      (1, BigInt(0)),
      (1, BigInt(1)),
      (1, BigInt(-1)),
      (1, BigInt(Int.MaxValue) + 1),
      (1, BigInt(Int.MinValue) - 1),
      (1, BigInt(Long.MaxValue)),
      (1, BigInt(Long.MinValue)),
      (1, BigInt(Long.MaxValue) + 1),
      (1, BigInt(Long.MinValue) - 1)
    )
    val mc = MathContext.DECIMAL128
    for {
      x <- genBigInt
      // Generates numbers outside the range of a Double without breaking the Jaxson parser.
      // I couldn't find the true source of the exception in Jaxson, but this should still cover most needs.
      scale <- Gen.chooseNum(10000, Int.MaxValue)
    } yield BigDecimal(x, scale, mc)
  }

  /**
   * Generates non-nested [[JsValue]]s (ie. not [[JsArray]] or [[JsObject]]).
   */
  def genJsPrimitive: Gen[JsValue] = {
    var genPrims: List[Gen[JsValue]] = List(
      arbJsBoolean.arbitrary,
      arbJsNumber.arbitrary,
      arbJsString.arbitrary,
      Gen.const(JsNull)
    )
    // A goofy way to match the signature of this method that I want, but it works
    Gen.oneOf(genPrims.head, genPrims.tail.head, genPrims.tail.tail: _*)
  }

  /**
   * Generates a primitive or nested [[JsValue]] up to the specified depth and width
   *
   * @param maxDepth see [[defaultMaxDepth]] (cannot be less than 0)
   * @param maxWidth see [[defaultMaxWidth]] (cannot be less than 0)
   */
  def genJsValue(implicit maxDepth: Depth = defaultMaxDepth, maxWidth: Width = defaultMaxWidth): Gen[JsValue] = {
    if (maxDepth === 0) genJsPrimitive
    else Gen.oneOf(
      genJsPrimitive,
      // The Scala compiler has a bug with AnyVal, where it favors implicits in the outer scope
      genJsArray(maxDepth, maxWidth),
      genJsObject(maxDepth, maxWidth)
    )
  }

  /**
   * Generates a nested array at the specified depth and width.
   *
   * @note the arrays may contain mixed type values at different depths, but never deeper than the [[defaultMaxDepth]].
   *
   * @param maxDepth see [[defaultMaxDepth]] (cannot be less than 1)
   * @param maxWidth see [[defaultMaxWidth]] (cannot be less than 1)
   */
  def genJsArray(implicit maxDepth: Depth = defaultMaxDepth, maxWidth: Width = defaultMaxWidth): Gen[JsArray] =
    Gen.listOfN(maxWidth, genJsValue(maxDepth - 1, maxWidth)) map { JsArray(_) }

  /**
   * Generates a valid field name where the first character is alphabetical and the remaining chars
   * are alphanumeric.
   */
  def genFieldName: Gen[String] = Gen.identifier

  def genFields(implicit maxDepth: Depth = defaultMaxDepth, maxWidth: Width = defaultMaxWidth): Gen[(String, JsValue)] = {
    // The Scala compiler has a bug with AnyVal, where it favors implicits in the outer scope
    Gen.zip(genFieldName, genJsValue(maxDepth, maxWidth))
  }

  /**
   * Generates a nested array at the specified depth and width.
   *
   * @param maxDepth see [[defaultMaxDepth]] (cannot be less than 1)
   * @param maxWidth see [[defaultMaxWidth]] (cannot be less than 1)
   */
  def genJsObject(implicit maxDepth: Depth = defaultMaxDepth, maxWidth: Width = defaultMaxWidth): Gen[JsObject] = {
    for {
      fields <- Gen.listOfN(maxWidth, genFields(maxDepth - 1, maxWidth))
    } yield JsObject(fields)
  }

  // Shrinks for better error output

  implicit val shrinkJsArray: Shrink[JsArray] = Shrink {
    arr =>
      val stream: Stream[JsArray] = shrink(arr.value) map JsArray
      stream
  }

  implicit val shrinkJsObject: Shrink[JsObject] = Shrink {
    obj =>
      val stream: Stream[JsObject] = shrink(obj.value) map { fields => JsObject(fields.toSeq) }
      stream
  }

  implicit val shrinkJsValue: Shrink[JsValue] = Shrink {
    case array: JsArray => shrink(array)
    case obj: JsObject  => shrink(obj)
    case JsString(str)  => shrink(str) map JsString
    case JsNumber(num)  => shrink(num) map JsNumber
    case JsNull | JsUndefined() | JsBoolean(_) => Stream.empty[JsValue]
  }
}

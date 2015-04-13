package play.api.libs.json.ops

import org.scalacheck.ops._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.FlatSpecLike
import play.api.libs.json._

import scala.reflect.ClassTag

/**
 * A common base class for providing a free serialization specification.
 *
 * This currently supports testing Lift and Play serialization, as well as cross-serialization
 * from a Lift serialized value read by Play and vice versa.
 *
 * Subclasses of this will test that for each example, the serialized result can be read back
 * into a value that is equal to the original example.
 */
sealed trait SerializationSpec[T] extends FlatSpecLike {

  def examples: Seq[T]

  protected implicit def clsTag: ClassTag[T]

  protected def className: String = clsTag.runtimeClass.getSimpleName

  protected def assertSame(actual: T, expected: T, prettyJson: String): Unit = {
    if (actual != expected) {
      fail(
        s"The actual and expected values are not equal.\n\n" +
          s"actual:\n$actual\n\n" +
          s"expected:\n$expected\n\n" +
          s"json:\n$prettyJson\n\n")
    }
  }
}

/**
 * A mixin for adding Play serialization tests.
 */
trait PlaySerialization[T] {
  self: SerializationSpec[T] =>

  protected implicit def playFormat: Format[T]

  protected def assertSame(actual: T, expected: T, decomposed: JsValue): Unit = {
    assertSame(actual, expected, Json.prettyPrint(decomposed))
  }

  s"Json of $className" should "read what it writes as JsValue" in {
    val decomposed = examples map playFormat.writes
    val reconstructed = decomposed map { written =>
      val pretty = Json.prettyPrint(written)
      val result = playFormat.reads(written)
      result match {
        case JsSuccess(extracted, _) => extracted
        case JsError(errors) =>
          val errorDetails = Json.prettyPrint(JsError.toFlatJson(errors))
          fail(
            s"Could not read an instance of $className from:\n$pretty\n\n" +
              s"Play detected errors:\n$errorDetails\n")
      }
    }
    for (((expected, actual), asWritten) <- examples zip reconstructed zip decomposed) {
      assertSame(actual, expected, asWritten)
    }
  }
}

/**
 * Extend this to perform only Play serialization tests.
 */
abstract class PlaySerializationSpec[T](
  override val examples: Seq[T]
)(implicit
  override protected implicit val playFormat: Format[T],
  override protected implicit val clsTag: ClassTag[T]
) extends SerializationSpec[T]
  with PlaySerialization[T] {

  def this(gen: Gen[T], samples: Int = 100)
    (implicit playFormat: Format[T], manifestT: Manifest[T]) =
    this(gen.toIterator.take(samples).toSeq)

  def this(samples: Int = 100)
    (implicit arb: Arbitrary[T], playFormat: Format[T], manifestT: Manifest[T]) =
    this(arb.arbitrary, samples)
}
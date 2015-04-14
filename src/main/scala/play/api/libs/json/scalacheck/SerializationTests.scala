package play.api.libs.json.scalacheck

import org.scalacheck.Shrink
import play.api.libs.json._

import scala.reflect.ClassTag
import scala.testing.{GenericTestSuite, TestSuiteBridge}
import scala.util.Try
import scala.util.control.{NoStackTrace, NonFatal}

/**
 * A common base class for providing a free serialization specification.
 *
 * This currently supports testing Lift and Play serialization, as well as cross-serialization
 * from a Lift serialized value read by Play and vice versa.
 *
 * Subclasses of this will test that for each example, the serialized result can be read back
 * into a value that is equal to the original example.
 */
trait SerializationTests[T] extends GenericTestSuite {
  self: TestSuiteBridge =>

  def examples: Seq[T]

  protected def shrink: Shrink[T]

  protected def clsTag: ClassTag[T]

  protected def className: String = clsTag.runtimeClass.getSimpleName

  protected def assertPostSerializationEquality(expected: T, actual: T): Unit = self.assertEqual(expected, actual)

  protected def assertSame(expected: T, actual: T, prettyJson: String): Unit = {
    try assertPostSerializationEquality(expected, actual)
    catch {
      case NonFatal(ex) =>
        fail(
          s"The expected and actual values are not equal.\n\n" +
          s"expected:\n$expected\n\n" +
          s"actual:\n$actual\n\n" +
          s"json:\n$prettyJson\n\n",
          Some(ex)
        )
    }
  }
}

/**
 * A mixin for adding Play serialization tests.
 */
trait PlaySerializationTests[T] extends SerializationTests[T] {
  self: TestSuiteBridge =>

  protected implicit def playFormat: Format[T]

  protected def assertSame(actual: T, expected: T, decomposed: JsValue): Unit = {
    assertSame(expected, actual, Json.prettyPrint(decomposed))
  }

  addTest(s"Format[$className] should read what it writes as JsValue in") {
    val decomposed = examples.par map playFormat.writes
    val reconstructed = decomposed map { written =>
      val pretty = Json.prettyPrint(written)
      val result = playFormat.reads(written)
      result match {
        case JsSuccess(extracted, _) => extracted
        case JsError(errors) =>
          val errorDetails = Json.prettyPrint(JsError.toFlatJson(errors))
          fail(
            s"Could not read an instance of $className from:\n$pretty\n\n" +
              s"Play detected errors:\n$errorDetails\n"
          )
      }
    }
    for (((expected, actual), asWritten) <- examples zip reconstructed zip decomposed) {
      assertSameWithShrink(expected, actual, asWritten)
    }
  }

  protected def assertSameWithShrink(expected: T, actual: T, asWritten: JsValue): Unit = {
    try assertSame(expected, actual, asWritten)
    catch {
      case NonFatal(originalEx) =>
        var lastEx = originalEx
        var shrinks = 0
        for (simpler <- shrink.shrink(expected)) {
          shrinks += 1
          val json = Try(playFormat.writes(simpler)) getOrElse { throw lastEx }
          val expected = Try(json as playFormat) getOrElse { throw lastEx }
          try assertSame(simpler, expected, json)
          catch {
            case NonFatal(ex) => lastEx = ex
          }
        }
        lastEx.addSuppressed(new RuntimeException(s"Applied $shrinks shrinks") with NoStackTrace)
        throw lastEx
    }
  }
}

/**
 * Extend this to perform Play serialization tests.
 *
 * This provides a sample constructor that is easy to fulfill in the subclass.
 */
abstract class PlayJsonFormatTests[T](
  override val examples: Seq[T],
  override protected implicit val playFormat: Format[T],
  override protected val clsTag: ClassTag[T],
  override protected val shrink: Shrink[T]
) extends PlaySerializationTests[T] {
   self: TestSuiteBridge =>

  /* sadly alternate constructors do not work with self-types properly in Scala 2.10,
   * and an implicit argument list would have the same signature after type erasure
   * so it is up to the subclasses to provide a nicer interface.
   **/

}
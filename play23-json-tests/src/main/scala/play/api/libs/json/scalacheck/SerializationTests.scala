package play.api.libs.json.scalacheck

import org.scalacheck.Shrink
import play.api.libs.json._

import scala.reflect.ClassTag
import scala.testing.{GenericTestSuite, TestSuiteBridge}
import scala.util.Try
import scala.util.control.{NoStackTrace, NonFatal}

/**
 * Mixin that adds some free serialization tests.
 *
 * This will add a test that verifies that every model written will be read back to a value that
 * is equivalent to the original example, and print a detailed error message if not.
 *
 * If you have a custom way of determining equality that lives outside of the model class itself,
 * you can override the methods used for comparison.
 */
trait SerializationTests[T] extends GenericTestSuite {
  self: TestSuiteBridge =>

  type Serialized

  def examples: Seq[T]

  protected def shrink: Shrink[T]

  protected def clsTag: ClassTag[T]

  protected def className: String = clsTag.runtimeClass.getSimpleName

  protected def serialize(model: T): Serialized

  /**
   * A function that either returns an error message or the deserialized model.
   */
  protected def deserialize(serialized: Serialized): Either[String, T]

  /**
   * Override this method to define a way to serialize the test json to a human-friendly format.
   */
  protected def prettyPrint(serialized: Serialized): String

  /**
   * Override this method if you want to define a different way to assert equality than the test
   * framework supplies by default.
   */
  protected def assertPostSerializationEquality(expected: T, actual: T): Unit = self.assertEqual(expected, actual)

  /**
   * Override this method if you want to print a different error on failure.
   */
  protected def assertSame(expected: T, actual: T, serialized: Serialized): Unit = {
    try assertPostSerializationEquality(expected, actual)
    catch {
      case NonFatal(exception) =>
        val prettyOutput = prettyPrint(serialized)
        fail(
          s"The expected and actual values are not equal.\n\n" +
          s"expected:\n$expected\n\n" +
          s"actual:\n$actual\n\n" +
          s"json:\n$prettyOutput\n\n",
          exception
        )
    }
  }

  /**
   * Uses the provided [[shrink]] to shrink a failing example to its simplest form that still fails the test.
   *
   * @note you can override this method to just call [[assertSame]] if you don't want to apply the shrink.
   */
  protected def assertSameWithShrink(expected: T, actual: T, serialized: Serialized): Unit = {
    try assertSame(expected, actual, serialized)
    catch {
      case NonFatal(originalEx) =>
        var lastEx = originalEx
        var shrinks = 0
        for (simpler <- shrink.shrink(expected)) {
          shrinks += 1
          val output = Try(serialize(simpler)) getOrElse { throw lastEx }
          val expected = Try(deserialize(output).right.get) getOrElse { throw lastEx }
          try assertSame(simpler, expected, output)
          catch {
            case NonFatal(ex) => lastEx = ex
          }
        }
        lastEx.addSuppressed(new RuntimeException(s"Applied $shrinks shrinks") with NoStackTrace)
        throw lastEx
    }
  }

  // Register the actual test
  addTest(s"Format[$className] should read what it writes as JsValue in") {
    val decomposed = examples.par map serialize
    val reconstructed = decomposed map { written =>
      val pretty = prettyPrint(written)
      val result = deserialize(written)
      result match {
        case Right(model) =>
          model
        case Left(errorMsg) =>
          fail(s"Could not read an instance of $className from:\n$pretty\n\nPlay detected errors:\n$errorMsg\n")
      }
    }
    for (((expected, actual), asWritten) <- examples zip reconstructed zip decomposed) {
      assertSameWithShrink(expected, actual, asWritten)
    }
  }
}

/**
 * A mixin for adding Play serialization tests.
 */
trait PlaySerializationTests[T] extends SerializationTests[T] {
  self: TestSuiteBridge =>

  override type Serialized = JsValue

  protected implicit def playFormat: Format[T]

  override protected def serialize(model: T): Serialized = Json.toJson(model)

  override protected def deserialize(serialized: Serialized): Either[String, T] = serialized.validate[T] match {
    case JsSuccess(model, _) => Right(model)
    case JsError(errors)     => Left(prettyPrint(JsError.toFlatJson(errors)))
  }

  override protected def prettyPrint(serialized: Serialized): String = Json.prettyPrint(serialized)
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
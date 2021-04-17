package play.api.libs.json.ops.v4

import play.api.libs.json._

import scala.reflect._
import scala.util.control.NonFatal

object FormatOps {

  /**
   * Extracts the class name of the [[Enumeration]] removing any `$` symbols without throwing an exception.
   *
   * @note this will include any outer object or class names separated by `.`s
   */
  def enumClassName(o: Enumeration): String = {
    ClassNameUtils.safeSimpleClassName(o.getClass)
  }

  /**
   * Creates a Format for the given enumeration's values by converting it to and from a string.
   *
   * @note You should avoid using an enumeration serializer to handle invalid input from an external
   *       API as this will force changes in the values to break compatibility.
   *
   *       The worst part about this is that adding enumeration values can break old clients and thus
   *       prevent new backwards-compatible, releases of the API from breaking this client.
   *
   *       Instead of validating enumerations for client-side code, consider using a plain old string,
   *       match on the known cases, and handle unknown cases in a manner that is safe for the future
   *       addition of values.
   *
   *       YOU HAVE BEEN WARNED.
   *
   * @param o the [[Enumeration]] object
   */
  def enumValueString[E <: Enumeration](o: E): Format[E#Value] = new Format[E#Value] {
    override def reads(json: JsValue): JsResult[E#Value] = json.validate[String].flatMap { s =>
      try JsSuccess(o.withName(s))
      catch {
        case NonFatal(_) =>
          val lowerCaseClassName  = enumClassName(o).toLowerCase
          JsError(s"error.expected.$lowerCaseClassName: No value found for '$s'")
      }
    }
    override def writes(o: E#Value): JsValue = JsString(o.toString)
  }

  /**
   * Creates a Format for the given type of value by converting it to and from a String.
   *
   * This is useful for serializing objects which can themselves be serialized as a String.
   * For example, this can be used for Encrypted values, String value classes, and serializing
   * complex objects as Strings for the purpose of packaging or compression.
   *
   * @param fromString a function to convert the value of type T to a String
   * @param toString a function for converting a String back to a value of type T
   */
  def asString[T](fromString: String => T, toString: T => String): Format[T] = {
    Format(
      Format.of[String] map fromString,
      Writes(it => JsString(toString(it)))
    )
  }


  /**
   * Creates a format for an empty collection type, such as Nil.type.
   *
   * Useful for avoiding compile-time errors when serializing empty collections,
   * where the compiler cannot infer the item type of the collection.
   *
   * @param empty the value when empty
   * @tparam C the empty collection type (can be a singleton)
   * @return a Format that will always write an empty JsArray() and read the given empty value.
   */
  def empty[C <: Iterable[Nothing]](empty: C): Format[C] = {
    new Format[C] {
      override def reads(json: JsValue): JsResult[C] = {
        Reads.traversableReads[Seq, JsValue].reads(json).flatMap {
          case Seq() => JsSuccess(empty)
          case _ => JsError("expected.jsarray.empty")
        }
      }
      override def writes(o: C): JsValue = JsArray()
    }
  }

  /**
   * Creates a Format that always reads the given value (regardless of the input)
   * and writes the given JsValue (regardless of the value given).
   *
   * @param value the pure value to always read.
   */
  def pure[T: Writes](value: => T): Format[T] = pure[T](value, Writes.of[T].writes(value))

  /**
   * Creates a Format that always reads the given value (regardless of the input)
   * and writes the given JsValue (regardless of the value given).
   *
   * @param value the pure value to always read.
   * @param json the pure json to always write.
   */
  def pure[T](value: => T, json: => JsValue): Format[T] = new Format[T] {
    override def reads(json: JsValue): JsResult[T] = JsSuccess(value)
    override def writes(o: T): JsValue = json
  }

  /**
   * When you want to build a formatter for some model that represents one of two possible formats or outcomes,
   * this helper will allow you to do so by supplying the two possible representations: typically success and failure.
   * You then call [[EitherFormatBuilder.from]] on this helper object, passing in the condition/conditions which dictate which
   * representation to use, which returns a [[Format]] object for the common parent class that both
   * success and failure models extend from.
   *
   * This helper should only be used if [[Failed]] and [[Success]] are the only two models which extend their
   * common parent model, otherwise runtime exceptions can occur.
   *
   * @tparam Failed The representation for one of the outcomes, usually the failure case, or the "Left" case
   * @tparam Success The representation for the other outcome, usually the success case, or the "Right" case
   * @return A helper class which can be used to build a [[Format]] from some condition
   * @see [[EitherFormatBuilder.from]]
   */
  def asEither[Failed: Format, Success: Format]: EitherFormatBuilder[Failed, Success] =
    new EitherFormatBuilder[Failed, Success]

  /**
   * @see [[asEither(Format[Failed], Format[Success])]]
   */
  final class EitherFormatBuilder[Left, Right] private[FormatOps] (
    implicit formatLeft: Format[Left], formatRight: Format[Right]
  ) {

    /**
     * Creates a [[Format]] from a single condition that returns a Boolean: true to use the "Right" or "Success"
     * format, and false to use the "Left" or "Failed" format.
     * Uses the supplied implicit ClassTags for dictating serialization.
     *
     * @param jsonIsRight a condition ([[JsValue]] => [[Boolean]]) when true, use the "Right" format, else use the
     *                    "Left" format
     * @param leftType The model which represents the "Left" or "Failed" case
     * @param rightType The model which represents the "Right" or "Success" case
     * @param leftAsX evidence which proves that leftType extends some common parent class X
     * @param rightAsX evidence which proves that rightType extends some common parent class X
     */
    def from[X](jsonIsRight: JsValue => Boolean)
      (implicit
        leftType: ClassTag[Left], rightType: ClassTag[Right],
        leftAsX: Left <:< X,      rightAsX: Right <:< X
        ): Format[X] = {
      from(jsonIsRight, {
        case leftType(_) => false
        case rightType(_) => true
        case otherwise =>
          throw new IllegalArgumentException(s"$otherwise is not an instance of either $leftType or $rightType.")
      })
    }

    /**
     * Creates a [[Format]] from two conditions, the first condition dictates how to deserialize the Json, and the
     * second format dictates how to serialize it. In both conditions, the "Left" or "Failed" format is false, and
     * the "Right" or "Success" format is true.
     *
     * @param jsonIsRight the condition which dictates how to deserialize the Json. If true, use the "Right" or
     *                    "Success" format, otherwise use the "Left" or "Failed" format
     * @param objectIsRight the condition which dictates how to serialize the object to Json. If true, use the "Right"
     *                      or "Success" format, otherwise use the "Left" or "Failed" format
     * @param leftAsX evidence which proves that leftType extends some common parent class X
     * @param rightAsX evidence which proves that rightType extends some common parent class X
     */
    def from[X](jsonIsRight: JsValue => Boolean, objectIsRight: X => Boolean)
      (implicit leftAsX: Left <:< X, rightAsX: Right <:< X): Format[X] = new Format[X] {

      override def reads(json: JsValue): JsResult[X] = {
        if (jsonIsRight(json))
          formatRight.reads(json) map rightAsX
        else
          formatLeft.reads(json) map leftAsX
      }

      override def writes(o: X): JsValue = {
        // Assuming X is a subtype of both Left and Right
        if (objectIsRight(o))
          formatRight.writes(o.asInstanceOf[Right])
        else
          formatLeft.writes(o.asInstanceOf[Left])
      }
    }
  }
}


package play.api.libs.json.ops

import play.api.libs.json.{JsError, JsSuccess, Reads, JsValue}

import scala.reflect.ClassTag

/**
 * Provides helper methods on [[JsValue]].
 *
 * @param json the original json value
 */
class JsValueOps(val json: JsValue) extends AnyVal {

  /**
   * Attempts to parse the result as the provided type or throws a helpful exception message.
   *
   * @note Sometimes you may want to redact sensitive information from the json in the
   *       exception message. To do this, just define an implicit JsonTransform for your
   *       specific type, and it will apply the transformer before putting the original
   *       json into the exception message.
   */
  def asOrThrow[A: Reads: ClassTag: JsonTransform]: A = json.validate[A] match {
    case JsSuccess(a, _) => a
    case err: JsError => throw InvalidJsonException[A](json, err)
  }

  /**
   * Adds another way to call [[JsValue.transform]], but based on type using the implicit [[JsonTransform]].
   *
   * Uses the implicit [[JsonTransform]] for this type to redact or alter the resulting [[JsValue]].
   *
   * @tparam A the type of value to transform this json for
   * @return a JsValue that has been altered based on the implicit transformer
   */
  def transformAs[A](implicit transformer: JsonTransform[A]): JsValue = transformer transform json
}
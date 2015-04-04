package play.api.libs.json.ops

import play.api.libs.json.ops.JsValueImplicits._
import play.api.libs.json._

import scala.reflect.ClassTag

/**
 * An implicit mechanism for transforming [[JsValue]]s before they are thrown in exceptions or printed
 * to the logs.
 *
 * This is the ideal way to redact data from Json, when you know the starting type or expected type
 * (in the case where you have failed to parse the model object and don't want to dump the json in
 * the error message).
 *
 * @param transform a function that will add, remove, or modify fields.
 * @tparam A a type-handle for selectively applying json transform functions.
 */
class JsonTransform[A](val transform: JsValue => JsValue) extends AnyVal

object JsonTransform {

  /**
   * The default value to replace on redacted fields.
   */
  val RedactedValue: JsValue = JsString("[REDACTED]")

  /**
   * Provides a default transformer for all types.
   *
   * @return a transformer that will pass the json through unchanged
   */
  implicit def defaultTransform[A: ClassTag]: JsonTransform[A] = JsonTransform[A]((x: JsValue) => x)

  def apply[A](transform: JsValue => JsValue): JsonTransform[A] = new JsonTransform[A](transform)

  def apply[A <: JsValue](implicit transform: Writes[JsValue]): JsonTransform[A] = new JsonTransform[A](transform.writes)

  def obj[A](field: JsValue => JsValue = _.as[JsObject])(transform: JsObject => JsObject): JsonTransform[A] =
    new JsonTransform[A]({
      case o: JsObject => transform(o)
      case v: JsValue  => field(v)
    })

  def redact[A](replace: JsValue => JsValue = _ => RedactedValue): JsonTransform[A] = {
    JsonTransform.redactAll[A] {
      case JsNull => JsNull
      case defined: JsValue => replace(defined)
    }
  }

  def redactAll[A](replace: JsValue => JsValue = _ => RedactedValue): JsonTransform[A] = JsonTransform(replace)

  def redactPaths[A](paths: Seq[JsPath], replace: JsValue = RedactedValue): JsonTransform[A] = {
    val redactedObj = JsPath.createObj(paths.map(_ -> replace): _*)
    JsonTransform.obj() { o =>
      o deepMerge redactedObj
    }
  }

  def transform[A: JsonTransform](json: JsValue): JsValue = json.transformAs[A]
}
package play.api.libs.json.ops

import play.api.libs.json._

import scala.reflect._
import scala.reflect.runtime.universe._

/**
 * An exception representing the failure to match a type key to the specific serializer
 * for that type.
 *
 * @param valueType the type object of the generic type that could not be serialized
 * @param keyValue the value of the key that was unmatched with a serializer
 * @param keyPath the path to the key
 * @param keyType the type object of the key
 */
class UnrecognizedTypeKey(
  valueType: Type,
  keyValue: Any,
  keyPath: JsPath,
  keyType: Type
  ) extends RuntimeException({
  val keyValueStr = keyValue match {
    case s: String => "\"%s\"".format(s)  // for some reason string interpolation goes wonky here in 2.10
    case _         => keyValue.toString   // this should work for most case classes and primitive types
  }
  val keyPathJson = Json.prettyPrint(Json.obj(keyPath.toJsonString -> keyType.toString))
  s"Cannot parse instance of $valueType. Unrecognized key '$keyValue' " +
    s"as extracted from key path:\n$keyPathJson\n" +
    s"Please define a matching case $keyValueStr => OFormat.of[X]  // where X <: $valueType"
})

/**
 * An exception that provides better error messaging than the standard [[JsResultException]].
 *
 * @note the constructor is private to prevent accidentally creating the exception without
 *       applying the appropriate implicit transform.
 *
 * @param json the json that was being parsed (note, this is printed verbatim, be sure to redact
 *             any sensitive information.
 * @param error the [[JsError]] encountered while parsing the json
 */
class InvalidJson private[InvalidJson] (val expectedClass: Class[_], val json: JsValue, val error: JsError)
  extends RuntimeException(
    s"Could not read an instance of ${expectedClass.getName} from:\n" +
      s"${Json.prettyPrint(json)}\n" +
      s"Errors encountered:\n" +
      s"${Json.prettyPrint(JsError.toFlatJson(error))}"
  )

object InvalidJson {

  /**
   * Builds an instance of [[InvalidJson]] applying the appropriate [[JsonTransform]].
   *
   * @param rawJson the json that was being parsed (before applying the transform)
   * @param error the [[JsError]] encountered while parsing the json
   * @tparam A the type that was attempting to be parsed
   */
  def apply[A: JsonTransform: ClassTag](rawJson: JsValue, error: JsError): InvalidJson = {
    val json = JsonTransform.transform(rawJson)
    new InvalidJson(classTag[A].runtimeClass, json, error)
  }
}

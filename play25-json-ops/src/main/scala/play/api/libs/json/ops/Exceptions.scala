package play.api.libs.json.ops

import play.api.libs.json._

import scala.reflect._
import scala.reflect.runtime.universe._

private[ops] object Exceptions {

  /**
    * Returns the estimated literal string of code you would use to match on this value.
    */
  def keyAsCodeLiteral(keyValue: Any): String = {
    keyValue match {
      case s: String => "\"%s\"".format(s) // for some reason string interpolation goes wonky here in Scala 2.11
      case _ => keyValue.toString // this should work for most case classes and primitive types
    }
  }

  /**
    * Returns the path to the key in a more code-friendly format.
    */
  def keyPathAsString(keyPath: JsPath): String = {
    val str = keyPath.toJsonString
    if (str == "obj") "__" else str
  }
}

/**
  * An exception representing the failure to match a type key to the specific serializer
  * for that type.
  *
  * @param valueType the type object of the generic type that could not be serialized
  * @param keyValue the value of the key that was unmatched with a serializer
  * @param keyPath the path to the key
  * @param keyType the type object of the key
  */
class UnrecognizedTypeKeyException(
  val valueType: Type,
  val keyValue: Any,
  val keyPath: JsPath,
  val keyType: Type
  ) extends RuntimeException({
  val keyValueStr = Exceptions.keyAsCodeLiteral(keyValue)
  val keyPathString = Exceptions.keyPathAsString(keyPath)
  val keyPathWithType = s"$keyPathString.as[$keyType]"
  s"Cannot parse instance of $valueType.\n" +
    s"Unrecognized key '$keyValue' as extracted at path with type $keyPathWithType\n" +
    "Please define the following in the Json.formatAbstract for this type:\n" +
    s"  case $keyValueStr => OFormat.of[_ <: $valueType]\n"
})

/**
  * Thrown when the type key cannot be deserialized from Json.
  *
  * @param valueType the type object of the generic type that could not be serialized
  * @param jsonKey the key as written in Json
  * @param readErrors the [[JsError]] values accumulated when parsing the type key
  */
class JsonTypeKeyReadException(
  val valueType: Type,
  val jsonKey: JsObject,
  val readErrors: JsError
) extends RuntimeException({
  val jsonKeyString = Json.prettyPrint(jsonKey)
  val readErrorsString = Json.prettyPrint(JsError.toFlatJson(readErrors))
  s"Cannot write an instance of $valueType.\n" +
    s"The model's key was writen as Json: $jsonKeyString\n" +
    s"but could not be converted into a type key because of the following validation errors: $readErrorsString\n" +
    "Please make sure this object can be parsed by exactly one of the readers in the defined by:\n" +
    s"  Json.extractTypeKeyOf[$valueType].usingKeyObject()\n"
})

/**
  * Thrown when the wrong [[OFormat]] is chosen in the partial function given to [[AbstractJsonOps.formatAbstract]].
  *
  * @param valueType the type object of the generic type that could not be serialized
  * @param keyValue the value of the key that was unmatched with a serializer
  * @param keyPath the path to the key
  * @param keyType the type object of the key
  * @param cause the [[ClassCastException]] that caused this exception
  */
class WrongAbstractFormatException(
  val valueType: Type,
  val keyValue: Any,
  val keyPath: JsPath,
  val keyType: Type,
  cause: ClassCastException
) extends RuntimeException({
  val keyPathString = Exceptions.keyPathAsString(keyPath)
  val keyPathWithType = s"$keyPathString.as[$keyType]"
  s"Cannot parse instance of $valueType.\n" +
    s"The OFormat chosen by the given key '$keyValue' as extracted at path with type $keyPathWithType " +
    "encountered a ClassCastException\n" +
    "Please be sure that the definition of Json.formatAbstract uses the correct OFormat for this type.\n"
}, cause)

/**
  * An exception that provides better error messaging than the standard [[JsResultException]].
  *
  * @note the constructor is private to prevent accidentally creating the exception without
  *       applying the appropriate implicit transform.
  * @param json the json that was being parsed (note, this is printed verbatim, be sure to redact
  *             any sensitive information.
  * @param error the [[JsError]] encountered while parsing the json
  */
class InvalidJsonException private[InvalidJsonException] (val expectedClass: Class[_], val json: JsValue, val error: JsError)
  extends RuntimeException(
    s"Could not read an instance of ${expectedClass.getName} from:\n" +
      s"${Json.prettyPrint(json)}\n" +
      s"Errors encountered:\n" +
      s"${Json.prettyPrint(JsError.toFlatJson(error))}"
  )

object InvalidJsonException {

  /**
    * Builds an instance of [[InvalidJsonException]] applying the appropriate [[JsonTransform]].
    *
    * @param rawJson the json that was being parsed (before applying the transform)
    * @param error the [[JsError]] encountered while parsing the json
    * @tparam A the type that was attempting to be parsed
    */
  def apply[A: JsonTransform: ClassTag](rawJson: JsValue, error: JsError): InvalidJsonException = {
    val json = JsonTransform.transform(rawJson)
    new InvalidJsonException(classTag[A].runtimeClass, json, error)
  }
}

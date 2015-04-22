package play.api.libs.json.ops

import play.api.libs.json._

import scala.annotation.implicitNotFound
import scala.language.implicitConversions
import scala.reflect.runtime.universe._

/**
 * Provides a mechanism for creating serializers for traits or abstract classes with multiple subclasses.
 *
 * The pattern works as follows:
 *
 * 1. Create the formats of each of the specific formats using [[AbstractJsonOps.formatWithType]]
 *    and the [[JsonMacroOps.oformat]] macro.
 *
 *    This will append the key field (even if it isn't in the case class constructor args) to the output json.
 *
 * 2. Create an implicit [[TypeKeyExtractor]] for the generic trait or abstract class on the companion object
 *    of that class.
 *
 *    This is required for the [[AbstractJsonOps.formatWithType]] to work properly and avoids repeating
 *    unnecessary boilerplate on each of the specific serializers to write out the key or the generic
 *    serializer to read the key.
 *
 * 3. Finally, define an implicit [[Format]] for your generic trait or abstract class using
 *    [[AbstractJsonOps.formatAbstract]] by providing a partial function from the extracted key (from #2)
 *    to the specific serializer (from #1). Any unmatched keys will throw an exception.
 *   
 * Usage:
 * {{{
 *   trait Generic {
 *     def key: String
 *   }
 *   
 *   object Generic extends JsonImplicits {
 *     implicit val extractor = Json.extractTypeKey[Generic].using(_.key, __ \ "key")
 *
 *     implicit val format = Json.formatAbstract[Generic] {
 *       case "specific" => OFormat.of[Specific]
 *       // other subclasses can be keyed into here using the value of the key field on the json / model object
 *     }
 *   }
 *
 *   case class Specific(value: Int) extends Generic {
 *     // this key will be written to the json even though the macro doesn't insert it
 *     def key = "specific"
 *   }
 *
 *   object Specific extends JsonImplicits {
 *     implicit val format = Json.formatWithType[Specific, Generic](Json.oformat[Specific])
 *   }
 * }}}
 */
object AbstractJsonOps extends JsonImplicits {

  /**
   * Creates an object serializer [[OFormat]] that also serialized a type key field.
   *
   * The type key field depends on the implicit [[TypeKeyExtractor]] provided.
   * By serializing this type key, you can use [[formatAbstract]] on a superclass
   * to match and deserialize this type appropriately.
   *
   * Usage:
   * {{{
   * object Specific extends JsonImplicits {
   *   implicit val format: Format[Specific] = Json.formatWithType[Specific, Generic](Json.oformat[Specific])
   * }
   * }}}
   *
   * @param objFormat the OFormat to add type info to
   * @tparam Concrete the concrete type to format this as
   * @tparam Abstract the abstract type for looking up the [[TypeKeyExtractor]]
   * @return an OFormat that serializes the concrete type with a given type key extractor.
   */
  def formatWithType[Concrete, Abstract >: Concrete : TypeKeyExtractor](objFormat: OFormat[Concrete]): OFormat[Concrete] = {
    new OFormat[Concrete] {
      private val extractor: TypeKeyExtractor[Abstract] = implicitly
      override def reads(json: JsValue): JsResult[Concrete] = objFormat.reads(json)
      override def writes(model: Concrete): JsObject = {
        val obj = objFormat.writes(model)
        val key = extractor.writeKeyToJson(extractor.readKeyFromModel(model))
        obj ++ key
      }
    }
  }

  /**
   * Creates an object serializer [[OFormat]] that also serialized a type key field.
   *
   * The type key field depends on the implicit [[TypeKeyExtractor]] provided.
   * By serializing this type key, you can use [[formatAbstract]] on a superclass
   * to match and deserialize this type appropriately.
   *
   * Usage:
   * {{{
   * object Specific extends JsonImplicits {
   *   implicit val format: Format[Specific] = Json.formatWithType[Specific](Generic.extractor, Json.oformat[Specific])
   * }
   * }}}
   *
   * @param objFormat the OFormat to add type info to
   * @tparam Concrete the concrete type to format this as
   * @return an OFormat that serializes the concrete type with a given type key extractor.
   */
  def formatWithType[Concrete](extractor: TypeKeyExtractor[_ >: Concrete], objFormat: OFormat[Concrete]): OFormat[Concrete] = {
    new OFormat[Concrete] {
      override def reads(json: JsValue): JsResult[Concrete] = objFormat.reads(json)
      override def writes(model: Concrete): JsObject = {
        val obj = objFormat.writes(model)
        val key = extractor.writeKeyToJson(extractor.readKeyFromModel(model))
        obj ++ key
      }
    }
  }

  /**
   * Creates an [[OFormat]] for a generic trait or abstract class by enumerating the formats
   * of all the subclasses.
   *
   * The format will examine a key field as extracted by the [[TypeKeyExtractor]], and then
   * it will use that format to finish the job of reading / writing the generic type.
   *
   * Usage:
   * {{{
   * trait Generic {
   *   def kind: String
   * }
   *
   * object Generic extends JsonImplicits {
   *   implicit val extractor: TypeKeyExtractor[Generic] = TypeKeyExtractor[Generic](_.kind)
   *
   *   implicit val format: Format[Generic] = Json.formatAbstract[Generic] {
   *     case Specific.kind => OFormat.of[Specific]
   *     // enumerating all specific subclasses
   *   }
   * }
   * }}}
   *
   * @param choose a partial function matching a key value as extracted from the json object
   *               to the specific [[OFormat]] that should be used to deserialize it
   * @tparam T the generic type for which to create a format
   * @return an OFormat that will deserialize the specific subclasses of [[T]] or will throw
   *         an [[UnrecognizedTypeKey]] exception, if the key is not matched by the partial function
   */
  def formatAbstract[T : TypeKeyExtractor](choose: PartialFunction[Any, OFormat[_ <: T]]): OFormat[T] =
    new OFormat[T] {

      /**
       * The bi-directional binding between Json type key and the associated type serializer.
       */
      private[this] val extractor: TypeKeyExtractor[T] = implicitly

      /**
       * A pre-compiled closure to grab the appropriate format for a given key, or None
       */
      private[this] val chooseOrNone: Any => Option[OFormat[_ <: T]] = choose.lift

      /**
       * Finds the appropriate format for a parsed key.
       *
       * @param typeKey the key used to lookup the format
       * @throws UnrecognizedTypeKey if the key has no corresponding format
       */
      private def findFormatOrThrow(typeKey: Any): OFormat[T] = {
        val format = chooseOrNone(typeKey) getOrElse extractor.throwUnrecognizedTypeKey(typeKey)
        // safe to cast since all subclasses of T should be writable from the provided format
        format.asInstanceOf[OFormat[T]]
      }

      override def reads(json: JsValue): JsResult[T] = {
        extractor.readKeyFromJson(json) flatMap { typeKey => findFormatOrThrow(typeKey).reads(json) }
      }

      override def writes(o: T): JsObject = {
        val typeKey = extractor.readKeyFromModel(o)
        val obj = findFormatOrThrow(typeKey).writes(o)
        val jsonKey = extractor.writeKeyToJson(typeKey)
        // TODO: Warn about overwritten keys?
        obj ++ jsonKey
      }
    }

  /**
   * Creates an immutable builder for creating a [[TypeKeyExtractor]] for a type.
   *
   * @note This doesn't actually create the extractor. It just captures the type of trait
   *       or abstract class, so that the [[TypeKeyDerivation.using]] method is easier to
   *       define without unnecessary type arguments.
   *
   * @tparam T the type of [[TypeKeyExtractor]] to build
   * @return a [[TypeKeyDerivation]] for building the final extrator
   */
  def extractTypeKey[T]: TypeKeyDerivation[T] = new TypeKeyDerivation[T]

  class TypeKeyDerivation[T] private[AbstractJsonOps] extends JsonImplicits {

    /**
     * Builds a [[TypeKeyExtractor]] for the captured type using the given function from
     * model to key value and the path in the json to that key.
     *
     * @note this requires and implicit [[Format]] of the key value type in order to parse
     *       the key from the given field in the json.
     *
     * @param getModelKey a function that extracts the key from the model
     * @param jsonKeyPath the path in the json to the key
     * @return a TypeKeyExtractor that uses the given functions to read and write the key
     *         to the output Json.
     */
    def using[K: TypeTag: Format](
      getModelKey: T => K,
      jsonKeyPath: JsPath
      )(implicit valueTag: TypeTag[T]): TypeKeyExtractor[T] =
      new TypeKeyExtractor[T] {
        override final type Key = K
        override def keyTypeTag: TypeTag[Key] = implicitly
        override val keyType: Type = keyTypeTag.tpe
        override def valueTypeTag: TypeTag[T] = valueTag
        override val valueType: Type = valueTypeTag.tpe
        override val keyPath: JsPath = jsonKeyPath
        override val formatAtKeyPath: Format[Key] = Format.of[K]
        override def readKeyFromModel(model: T): Key = getModelKey(model)
      }
  }
}

/**
 * A set of functions that enables reading and writing a type key to Json for the purpose of serializing
 * a trait or abstract class to and from json.
 *
 * In order to do this you need (1) a function that takes some model of the abstract type and returns a key,
 * (2) a function that takes the key and returns a specific serializer for that model, and (3) a function
 * to write the type key into the output json of each of the specific serializers.
 *
 * This class handles (1) and (3) and enables (2) by providing access to the key.
 *
 * In other words, this acts as a bi-directional binding between the key stored on the generic type [[T]]
 * and the key stored in json. All that remains is to provide the function from key to specific serializer.
 */
@implicitNotFound("You must define an implicit TypeKeyExtractor[${T}] in scope.  " +
  "Try adding `implicit val extractor = Json.deriveTypeKey[${T}].using(_.keyField, __ \\ \"keyPath\")` " +
  "to the companion object of ${T}")
abstract class TypeKeyExtractor[T] private[ops] {

  /**
   * The runtime type of the key value after it is extracted from the [[JsValue]].
   *
   * This key is used by [[AbstractJsonOps.formatAbstract]] to find the right [[OFormat]] to
   * use when parsing the Json as an abstract class or trait.
   */
  type Key

  /**
   * The [[TypeTag]] of the [[Key]]. Used for better error messages.
   */
  def keyTypeTag: TypeTag[Key]

  /**
   * The [[Type]] of [[Key]]. Used for better error messages.
   *
   * @note this is a val to force the evaluation of Type as early as possible since Scala's
   *       reflection API is not thread-safe. Once this is resolved, it could be calculated
   *       from the keyTypeTag in a lazy val.
   */
  val keyType: Type

  /**
   * The [[TypeTag]] of the value to [[Format]]. Used for better error messages.
   */
  def valueTypeTag: TypeTag[T]

  /**
   * The [[Type]] of the value to [[Format]]. Used for better error messages.
   *
   * @note this is a val to force the evaluation of Type as early as possible since Scala's
   *       reflection API is not thread-safe. Once this is resolved, it could be calculated
   *       from the keyTypeTag in a lazy val.
   */
  val valueType: Type

  /**
   * The [[JsPath]] to the key field.
   *
   * @note this only allows a single field to be used to determine the type. This was a
   *       design choice to avoid too much complexity in serialization logic.
   */
  def keyPath: JsPath

  /**
   * The format used to parse the key at the [[keyPath]].
   *
   * @note this is protected to avoid leaking this into the public API.
   */
  protected val formatAtKeyPath: Format[Key]

  /**
   * The single [[OFormat]] used for reading and writing the [[Key]]
   */
  final protected lazy val keyFormat: OFormat[Key] = keyPath format formatAtKeyPath

  /**
   * Reads the key field off of the model object.
   * @return the key used to look up the specific serializer
   */
  def readKeyFromModel(model: T): Key

  /**
   * Reads the key field off of the json.
   * @return the key used to look up the specific serializer
   */
  def readKeyFromJson(json: JsValue): JsResult[Key] = keyFormat reads json

  /**
   * Writes the key field to the output json.
   * @return the json object to be combined with the output json
   */
  def writeKeyToJson(key: Key): JsObject = keyFormat writes key

  /**
   * Throws an [[UnrecognizedTypeKey]] exception with all the appropriate arguments.
   * @param key the parsed key that has no corresponding [[OFormat]] for the specific
   *            type associated with it
   */
  def throwUnrecognizedTypeKey(key: Any): Nothing = {
    throw new UnrecognizedTypeKey(valueType, key, keyPath, keyType)
  }
}

object TypeKeyExtractor {

  def of[T: TypeKeyExtractor]: TypeKeyExtractor[T] = implicitly
}

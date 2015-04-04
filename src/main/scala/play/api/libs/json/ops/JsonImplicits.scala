package play.api.libs.json.ops

import play.api.libs.json._

import scala.language.implicitConversions

trait JsonImplicits extends ImplicitTupleFormats with JsValueImplicits {

  implicit def jsonMacroOps(json: Json.type): JsonMacroOps.type = JsonMacroOps

  implicit def formatOps(format: Format.type): FormatOps.type = FormatOps

  implicit def oformatOps(oformat: OFormat.type): OFormatOps.type = OFormatOps

  implicit def abstractFormatOps(json: Json.type): AbstractJsonOps.type = AbstractJsonOps

  /**
   * Provides a conversion for a format for generic Map[K, V]. Must have a Format[V] in scope.
   *
   * @param readKey a function that converts String to K or throws an exception if the conversion cannot be made.
   * @param writeKey a function that converts K to a String.
   */
  implicit def formatMap[K, V: Format](
    implicit readKey: String => K, writeKey: K => String = (_: K).toString): Format[Map[K, V]] = {
    val mapReads = Reads.mapReads[V]
    val mapWrites = Writes.mapWrites[V]
    Format(
      mapReads.map(mapWithStringKeys => mapWithStringKeys.map { case (k, v) => readKey(k) -> v }),
      Writes(mapWithKKeys => mapWrites.writes(mapWithKKeys.map { case (k, v) => writeKey(k) -> v }))
    )
  }
}

package play.api.libs.json.ops

import play.api.libs.json._
import play.api.libs.json.ops.JsonImplicits.JsonOps

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.reflect.macros.Context

trait JsonImplicits extends ImplicitTupleFormats {

  implicit def jsonOps(json: Json.type): JsonOps.type = JsonOps

  implicit def formatOps(format: Format.type): FormatOps.type = FormatOps

  implicit def oformatOps(oformat: OFormat.type): OFormatOps.type = OFormatOps

  implicit def abstractFormatOps(format: Format.type): FormatAbstractOps.type = FormatAbstractOps

  implicit def abstractOFormatOps(oformat: OFormat.type): FormatAbstractOps.type = FormatAbstractOps

  /**
   * Provides a conversion for a format for generic Map[K, V]. Must have a Format[V] in scope.
   *
   * @param readKey a function that converts String to K or throws an exception if the conversion cannot be made.
   * @param writeKey a function that converts K to a String.
   */
  implicit def formatMap[K, V: Format](
    implicit
    readKey: String => K,
    writeKey: K => String = (_: K).toString): Format[Map[K, V]] = {
    val mapFormat = Format(Reads.mapReads[V], Writes.mapWrites[V])
    Format(
      mapFormat.map(mapWithStringKeys => mapWithStringKeys.map { case (k, v) => readKey(k) -> v }),
      Writes(mapWithKKeys => mapFormat.writes(mapWithKKeys.map { case (k, v) => writeKey(k) -> v }))
    )
  }
}

object JsonImplicits {

  object JsonOps {

    /**
     * Creates a [[Format]] at compile time with Play's Json.format macro, but returns it as an
     * [[OFormat]]. [[OFormat]]s have writes that return [[JsObject]], which will always be the case for case classes.
     * Play will always actually return an [[OFormat]] instance, but for some reason they chose to
     * return it as [[Format]] instead (not sure why). This macro basically just hides the ugly task of performing an
     * asInstanceOf[OFormat] which you can assume is always safe.
     *
     */
    def oformat[A]: OFormat[A] = macro JsMacroImplOps.oformatImpl[A]
  }

  object JsMacroImplOps {
    def oformatImpl[A: c.WeakTypeTag](c: Context): c.Expr[OFormat[A]] = {
      import c.universe._
      val expFormatA = JsMacroImpl.formatImpl[A](c)
      reify {
        expFormatA.splice.asInstanceOf[OFormat[A]]
      }
    }
  }
}
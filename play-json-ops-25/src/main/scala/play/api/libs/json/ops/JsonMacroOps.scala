package play.api.libs.json.ops

import play.api.libs.json._

import scala.language.experimental.macros

object JsonMacroOps {

  /**
   * Creates a [[Format]] at compile time with Play's [[Json.format]] macro, but returns it as an [[OFormat]].
   *
   * [[OFormat]]s have writes that return [[JsObject]] instead of [[JsValue]], which will always be correct
   * for any macro based case class [[Format]].
   *
   * Play will always actually return an [[OFormat]] instance, but for some reason they chose to
   * return it as [[Format]] instead (not sure why). This macro basically just hides the ugly task of
   * calling .asInstanceOf[OFormat] which you can safely assume is always the case.
   */
  @deprecated("Use standard play.api.libs.json.Json.format[A]", "1.5.0")
  def oformat[A]: OFormat[A] = macro JsMacroImpl.formatImpl[A]

  /**
   * Creates a [[Writes]] at compile time with Play's [[Json.writes]] macro, but returns it as an [[OWrites]].
   *
   * [[OWrites]]s have writes that return [[JsObject]] instead of [[JsValue]], which will always be correct
   * for any macro based case class [[Writes]].
   *
   * Play will always actually return an [[OWrites]] instance, but for some reason they chose to
   * return it as [[Writes]] instead (not sure why). This macro basically just hides the ugly task of
   * calling .asInstanceOf[OWrites] which you can safely assume is always the case.
   */
  @deprecated("Use standard play.api.libs.json.Json.writes[A]", "1.5.0")
  def owrites[A]: OWrites[A] = macro JsMacroImpl.writesImpl[A]
}

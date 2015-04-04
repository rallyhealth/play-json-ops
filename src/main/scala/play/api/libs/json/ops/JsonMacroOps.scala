package play.api.libs.json.ops

import play.api.libs.json._

import scala.language.experimental.macros
import scala.reflect.macros.Context

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
  def oformat[A]: OFormat[A] = macro JsonMacroImpl.oformatImpl[A]

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
  def owrites[A]: OWrites[A] = macro JsonMacroImpl.owritesImpl[A]
}

object JsonMacroImpl {

  /**
   * A simple macro that just calls .asInstanceOf on the [[Json.format]] generated format.
   */
  def oformatImpl[A: c.WeakTypeTag](c: Context): c.Expr[OFormat[A]] = {
    import c.universe._
    val expFormatA = JsMacroImpl.formatImpl[A](c)
    reify {
      expFormatA.splice.asInstanceOf[OFormat[A]]
    }
  }

  /**
   * A simple macro that just calls .asInstanceOf on the [[Json.writes]] generated writer.
   */
  def owritesImpl[A: c.WeakTypeTag](c: Context): c.Expr[OWrites[A]] = {
    import c.universe._
    val expFormatA = JsMacroImpl.writesImpl[A](c)
    reify {
      expFormatA.splice.asInstanceOf[OWrites[A]]
    }
  }
}
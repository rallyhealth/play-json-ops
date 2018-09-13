package play.api.libs.json.ops

import play.api.libs.json.JsValue

import scala.language.implicitConversions

private[ops] object CompatibilityImplicits extends CompatibilityImplicits
private[ops] trait CompatibilityImplicits {

  implicit def asJsLookupLike(legacyLookup: JsValue): JsLookupCompat = new JsLookupCompat(legacyLookup)
}

class JsLookupCompat(protected val legacyLookup: JsValue) extends AnyVal {

  def get: JsValue = legacyLookup
}

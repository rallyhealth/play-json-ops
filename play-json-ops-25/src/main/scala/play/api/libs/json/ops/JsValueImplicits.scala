package play.api.libs.json.ops

import play.api.libs.json._

import scala.language.implicitConversions

trait JsValueImplicits {

  implicit def jsValueOps(json: JsValue): JsValueOps = new JsValueOps(json)
}

@deprecated("extend JsValueImplicits or import play.api.libs.json.ops._", "0.2.2")
object JsValueImplicits extends JsValueImplicits

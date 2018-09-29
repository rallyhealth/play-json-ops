package play.api.libs.json.ops

import play.api.libs.json._

import scala.language.implicitConversions

trait JsValueImplicits {

  implicit def jsValueOps(json: JsValue): JsValueOps = new JsValueOps(json)
}

package play.api.libs.json.ops

import play.api.libs.json._

import scala.language.implicitConversions

private[ops] class JsonImplicits private[ops] extends ImplicitTupleFormats {

  implicit def jsValueOps(json: JsValue): JsValueOps = new JsValueOps(json)

  implicit def formatOps(format: Format.type): FormatOps.type = FormatOps

  implicit def oformatOps(oformat: OFormat.type): OFormatOps.type = OFormatOps

  implicit def abstractJsonOps(json: Json.type): AbstractJsonOps.type = AbstractJsonOps

  implicit def abstractJsonOps(json: TypeKeyExtractor.type): AbstractJsonOps.type = AbstractJsonOps

  implicit def readsMap[K: ReadsKey, V: Reads]: Reads[Map[K, V]] = {
    val readsK = ReadsKey.of[K]
    val stringKeyReader = Reads.map[V]
    stringKeyReader.flatMap { a =>
      Reads[Map[K, V]] { _ =>
        val initResult: JsResult[Map[K, V]] = JsSuccess(Map())
        a.map { case (k, v) => (readsK.read(k), v) }.foldLeft(initResult) {
          case (JsSuccess(acc, _), (JsSuccess(k, _), v)) => JsSuccess(acc.updated(k, v))
          case (JsSuccess(_, _), (firstError: JsError, _)) => firstError
          case (accErrors: JsError, (errors: JsError, _)) => accErrors ++ errors
          case (accErrors: JsError, _) => accErrors
        }
      }
    }
  }

  implicit def writesMap[K: WritesKey, V: Writes]: Writes[Map[K, V]] = {
    val writesK = WritesKey.of[K]
    val stringKeyWriter = Writes.map[V]
    Writes[Map[K, V]](values => stringKeyWriter.writes(values.map { case (k, v) => (writesK.write(k), v) }))
  }
}

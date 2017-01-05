package play.api.libs.json.ops

import play.api.libs.json._

object OFormatOps extends OFormatPure {

  def of[T: OFormat]: OFormat[T] = implicitly
}

trait OFormatPure {

  /**
   * Creates a Format that always reads the given value (regardless of the input)
   * and writes the given JsValue (regardless of the value given).
   *
   * @param value the pure value to always read.
   */
  def pure[T](value: => T)(implicit writer: OWrites[T]): OFormat[T] = pure[T](value, writer.writes(value))

  /**
   * Creates a Format that always reads the given value (regardless of the input)
   * and writes the given JsValue (regardless of the value given).
   *
   * @param value the pure value to always read.
   * @param json the pure json to always write.
   */
  def pure[T](value: => T, json: => JsObject): OFormat[T] = new OFormat[T] {
    override def reads(json: JsValue): JsResult[T] = JsSuccess(value)
    override def writes(o: T): JsObject = json
  }
}

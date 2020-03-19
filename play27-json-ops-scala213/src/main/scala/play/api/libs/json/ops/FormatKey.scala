package play.api.libs.json.ops

import play.api.libs.json.JsResult

trait FormatKey[A] extends ReadsKey[A] with WritesKey[A]

object FormatKey {

  def of[A](implicit formatKey: FormatKey[A]): FormatKey[A] = formatKey

  implicit def apply[A](implicit readsKey: ReadsKey[A], writesKey: WritesKey[A]): FormatKey[A] = new FormatKey[A] {
    final override def write(key: A): String = writesKey.write(key)
    final override def read(key: String): JsResult[A] = readsKey.read(key)
  }
}
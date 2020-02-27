package play.api.libs.json.ops

import java.util.UUID

import play.api.libs.json.{JsError, JsResult, JsSuccess}

trait ReadsKey[A] { self =>

  /**
    * Read the string value into a typed key or return ignore this invalid key None.
    */
  def read(key: String): JsResult[A]

  final def map[B](f: A => B): ReadsKey[B] = new ReadsKey[B] {
    final override def read(key: String): JsResult[B] = self.read(key).map(f)
  }

  final def flatMap[B](f: A => ReadsKey[B]): ReadsKey[B] = new ReadsKey[B] {
    final override def read(key: String): JsResult[B] = self.read(key).flatMap(a => f(a).read(key))
  }
}

object ReadsKey {

  def of[A](implicit readsKey: ReadsKey[A]): ReadsKey[A] = readsKey

  def apply[A](fn: String => JsResult[A]): ReadsKey[A] = new ReadsKey[A] {
    override def read(key: String): JsResult[A] = fn(key)
  }

  private[this] def readsKeyNumber[A](f: String => A): ReadsKey[A] = new ReadsKey[A] {
    final def read(key: String): JsResult[A] = try JsSuccess(f(key)) catch {
      case ex: NumberFormatException => JsError(ex.getMessage)
    }
  }

  /**
    * A [[ReadsKey]] that will always succeed.
    */
  private[ops] abstract class AlwaysReadsKey[A] extends ReadsKey[A] {
    def readSafe(key: String): A
    final override def read(key: String): JsResult[A] = JsSuccess(readSafe(key))
  }

  implicit val readKeyString: ReadsKey[String] = new AlwaysReadsKey[String] {
    final override def readSafe(key: String): String = key
  }

  implicit val readKeySymbol: ReadsKey[Symbol] = new AlwaysReadsKey[Symbol] {
    final override def readSafe(key: String): Symbol = Symbol(key)
  }

  implicit val readKeyUUID: ReadsKey[UUID] = new ReadsKey[UUID] {
    final override def read(key: String): JsResult[UUID] = {
      if (key.length == 36) {
        try JsSuccess(UUID.fromString(key)) catch {
          case _: IllegalArgumentException => JsError("Invalid UUID format")
        }
      } else JsError("Invalid UUID length")
    }
  }

  implicit val readKeyByte: ReadsKey[Byte] = readsKeyNumber(java.lang.Byte.parseByte)
  implicit val readKeyShort: ReadsKey[Short] = readsKeyNumber(java.lang.Short.parseShort)
  implicit val readKeyInt: ReadsKey[Int] = readsKeyNumber(java.lang.Integer.parseInt)
  implicit val readKeyLong: ReadsKey[Long] = readsKeyNumber(java.lang.Long.parseLong)
}
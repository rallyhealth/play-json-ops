package play.api.libs.json.ops

import java.util.UUID

trait WritesKey[A] { self =>

  /**
    * Write the given key value as a string.
    */
  def write(key: A): String

  final def contramap[B](f: B => A): WritesKey[B] = new WritesKey[B] {
    final override def write(key: B): String = self.write(f(key))
  }
}

object WritesKey {

  def of[A](implicit writesKey: WritesKey[A]): WritesKey[A] = writesKey

  def apply[A](fn: A => String): WritesKey[A] = new WritesKey[A] {
    final override def write(key: A): String = fn(key)
  }
  
  implicit val writesKeyString: WritesKey[String] = new WritesKey[String] {
    final override def write(key: String): String = key
  }

  implicit val writesKeySymbol: WritesKey[Symbol] = new WritesKey[Symbol] {
    final override def write(key: Symbol): String = key.name
  }

  implicit val writesKeyUUID: WritesKey[UUID] = new WritesKey[UUID] {
    final override def write(key: UUID): String = key.toString
  }

  implicit val writesKeyByte: WritesKey[Byte] = new WritesKey[Byte] {
    final override def write(key: Byte): String = java.lang.Byte.toString(key)
  }

  implicit val writesKeyShort: WritesKey[Short] = new WritesKey[Short] {
    final override def write(key: Short): String = java.lang.Short.toString(key)
  }

  implicit val writesKeyInt: WritesKey[Int] = new WritesKey[Int] {
    final override def write(key: Int): String = java.lang.Integer.toString(key)
  }

  implicit val writesKeyLong: WritesKey[Long] = new WritesKey[Long] {
    final override def write(key: Long): String = java.lang.Long.toString(key)
  }
}

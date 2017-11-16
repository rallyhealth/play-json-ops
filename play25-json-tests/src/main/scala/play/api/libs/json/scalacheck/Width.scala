package play.api.libs.json.scalacheck

import scala.language.implicitConversions

class Width private[Width] (val width: Int) extends AnyVal with Counted with Proxy {
  override def self = width
  override protected def throwOnNegative(): Nothing = throw new IllegalArgumentException("Width cannot be negative")
  @inline override def count: Int = width
  def -(that: Width) = Width(this.width - that.width)
  def +(that: Width) = new Width(this.width + that.width)  // no need to validate
}

object Width extends (Int => Width) {

  implicit def fromInt(int: Int): Width = Width(int)

  implicit def toInt(width: Width): Int = width.width

  override def apply(width: Int): Width = {
    val w = new Width(width)
    w.validate()
    w
  }
}
package play.api.libs.json.scalacheck

import scala.language.implicitConversions

class Depth private[Depth] (val depth: Int) extends AnyVal with Counted with Proxy {
  override def self = depth
  override protected def throwOnNegative(): Nothing = throw new IllegalArgumentException("Depth cannot be negative")
  @inline override def count: Int = depth
  def -(that: Depth) = Depth(this.depth - that.depth)
  def +(that: Depth) = new Depth(this.depth + that.depth)  // no need to validate
}

object Depth extends (Int => Depth) {

  implicit def fromInt(int: Int): Depth = Depth(int)

  implicit def toInt(depth: Depth): Int = depth.depth

  override def apply(depth: Int): Depth = {
    val d = new Depth(depth)
    d.validate()
    d
  }
}
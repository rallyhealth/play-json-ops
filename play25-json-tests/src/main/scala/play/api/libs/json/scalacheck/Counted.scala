package play.api.libs.json.scalacheck

trait Counted extends Any {

  def count: Int

  protected def throwOnNegative(): Nothing

  def validate(): Unit = {
    count match {
      case neg if neg < 0 => throwOnNegative()
      case pos =>
    }
  }

  def <(that: Int) = this.count < that

  def <=(that: Int) = this.count <= that

  def >(that: Int) = this.count > that

  def >=(that: Int) = this.count >= that

  def ===(that: Int) = this.count == that

}

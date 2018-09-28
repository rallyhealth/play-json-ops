package play.api.libs.json

package object ops extends JsonImplicits {

  /**
    * DEPRECATED AND UNSAFE
    *
    * @note this overrides the implicit method to avoid low-priority implicit rules.
    *
    * @see [[JsonImplicits.formatMap]] for more details.
    */
  @deprecated("This method uses an unsafe call to .toString by default. " +
    "It will be removed in the next version.", "1.1.0")
  override def formatMap[K, V: Format](
    implicit readKey: String => K, writeKey: K => String = (_: K).toString): Format[Map[K, V]] = {
    super.formatMap
  }

  /**
    * Invalidates [[formatMap]] to avoid unsafe behavior by creating ambiguous implicits.
    */
  @deprecated("This method is only used to invalidate the implicit resolution of JsonImplicits.formatMap. " +
    "It will be removed in the next version.", "1.1.0")
  implicit def invalidateBuggyAndUnsafeFormatMapImplicit1[K, V](implicit readKey: String => K): Format[Map[K, V]] = {
    throw new UnsupportedOperationException(
      "This method is only used to invalidate the implicit resolution of JsonImplicits.formatMap."
    )
  }

  /**
    * Invalidates [[formatMap]] to avoid unsafe behavior by creating ambiguous implicits.
    */
  @deprecated("This method is only used to invalidate the implicit resolution of JsonImplicits.formatMap. " +
    "It will be removed in the next version.", "1.1.0")
  implicit def invalidateBuggyAndUnsafeFormatMapImplicit2[K, V](implicit readKey: String => K): Format[Map[K, V]] = {
    throw new UnsupportedOperationException(
      "This method is only used to invalidate the implicit resolution of JsonImplicits.formatMap."
    )
  }
}

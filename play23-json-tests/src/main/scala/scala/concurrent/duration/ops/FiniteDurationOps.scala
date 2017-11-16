package scala.concurrent.duration.ops

import scala.concurrent.duration._

import scala.language.implicitConversions

/**
 * Provides useful operations on [[FiniteDuration]]s.
 */
object FiniteDurationOps {

  /**
   * Same as [[Duration.toUnit]] except avoids loss of precision by not converting to Double first.
   *
   * @param duration the duration to convert
   * @param unit the [[TimeUnit]] to convert it to
   */
  def convertToUnitPrecisely(duration: FiniteDuration, unit: TimeUnit): FiniteDuration = {
    val magnitude = unit match {
      case DAYS         => duration.toDays
      case HOURS        => duration.toHours
      case MICROSECONDS => duration.toMicros
      case MILLISECONDS => duration.toMillis
      case MINUTES      => duration.toMinutes
      case NANOSECONDS  => duration.toNanos
      case SECONDS      => duration.toSeconds
    }
    Duration(magnitude, unit)
  }
}

/**
 * Provides additional operations on [[FiniteDuration]] with no runtime allocation overhead.
 */
class FiniteDurationOps(val duration: FiniteDuration) extends AnyVal {

  /**
   * Drops any extra precision that would be unrepresentable in the current Duration's [[TimeUnit]].
   */
  def dropInsignificantDigits: FiniteDuration = toUnitPrecise(duration.unit)

  /**
   * Converts a [[FiniteDuration]] to the provided unit without any loss of precision.
   *
   * @note this method has no affect on [[Duration.Infinite]] values
   *
   * @param unit the [[TimeUnit]], such as [[DAYS]], [[HOURS]], etc
   * @return a new [[FiniteDuration]] with the provided unit or the given [[Duration.Infinite]]
   */
  def toUnitPrecise(unit: TimeUnit): FiniteDuration = FiniteDurationOps.convertToUnitPrecisely(duration, unit)
}

/**
 * Extend this trait to get additional operations on [[FiniteDuration]]s.
 *
 * These are also made available by adding `import scala.concurrent.duration.ops._`
 */
trait DurationImplicits {

  implicit def fromFiniteDuration(duration: FiniteDuration): FiniteDurationOps =
    new FiniteDurationOps(duration)
}

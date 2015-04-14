package scala.concurrent.duration.ops

import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

object DurationOps {

  /**
   * This duplicates the logic from [[Duration.apply(s: String)]] but does not give up any precision.
   *
   * @note calling [[Duration.toString]] on x should always yield Some(x) with the same length and duration.
   *
   * @param durationString a string to parse a [[Duration]] from (typically from Duration.toString)
   * @return the first [[Duration]] value parsed from a given string
   */
  def parseLossless(durationString: String): Try[Duration] = {
    durationString.dropWhile(_.isWhitespace).toLowerCase match {
      case "inf" | "plusinf" | "+inf" => Success(Duration.Inf)
      case "minusinf" | "-inf"        => Success(Duration.MinusInf)
      case "undefined"                => Success(Duration.Undefined)
      case trimmed =>
        Try {
          trimmed.takeWhile {
            case '-' | '+' => true
            case x if x.isDigit => true
            case _ => false
          }.toLong
        } flatMap { len =>
          parseUnit(trimmed) match {
            case Some(unit) => Success(Duration(len, unit))
            case None =>
              Failure(new NumberFormatException(s"missing unit from duration '$durationString'"))
          }
        }
    }
  }

  /**
   * This duplicates the logic from [[Duration.apply(s: String)]] but only parses the unit.
   *
   * Grabs the first [[TimeUnit]] from a string, but only compares whole words as separated
   * by whitespace, numbers, or punctuation.
   *
   * @note calling [[FiniteDuration.toString]] on x should always return a Some(x.unit)
   *
   * @param durationString a string to parse a [[TimeUnit]] from (typically from Duration.toString)
   * @return the first [[TimeUnit]] value parsed from a given string
   */
  def parseUnit(durationString: String): Option[TimeUnit] = {
    val trimmed: String = durationString dropWhile (!_.isLetter)
    val unitName = trimmed takeWhile (_.isLetter)
    timeUnit get unitName
  }

  // "ms milli millisecond" -> List("ms", "milli", "millis", "millisecond", "milliseconds")
  private[this] def words(s: String) = (s.trim split "\\s+").toList
  private[this] def expandLabels(labels: String): List[String] = {
    val hd :: rest = words(labels)
    hd :: rest.flatMap(s => List(s, s + "s"))
  }
  private[this] val timeUnitLabels = List(
    DAYS         -> "d day",
    HOURS        -> "h hour",
    MINUTES      -> "min minute",
    SECONDS      -> "s sec second",
    MILLISECONDS -> "ms milli millisecond",
    MICROSECONDS -> "µs micro microsecond",
    NANOSECONDS  -> "ns nano nanosecond"
  )

  private[this] val timeUnit: Map[String, TimeUnit] =
    timeUnitLabels.flatMap { case (unit, names) => expandLabels(names) map (_ -> unit) }.toMap
}

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

trait DurationImplicits {

  implicit def fromFiniteDuration(duration: FiniteDuration): FiniteDurationOps =
    new FiniteDurationOps(duration)
}

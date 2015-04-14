package play.api.libs.json.scalacheck

import org.scalacheck.{Arbitrary, Gen}

import scala.concurrent.duration._
import scala.concurrent.duration.ops._
import scala.language.implicitConversions

trait DurationGenerators {

  implicit val arbTimeUnit: Arbitrary[TimeUnit] = Arbitrary {
    Gen.oneOf(
      DAYS,
      HOURS,
      MICROSECONDS,
      MILLISECONDS,
      MINUTES,
      NANOSECONDS,
      SECONDS
    )
  }

  /**
   * [[Duration]] doesn't support unsigned operations to allow unary - to be symmetric
   */
  private val minNanos = Long.MinValue + 1
  private val maxNanos = Long.MaxValue

  implicit def arbFiniteDuration(implicit timeUnitArb: Arbitrary[TimeUnit]): Arbitrary[FiniteDuration] = Arbitrary {
    for {
      length <- Gen.chooseNum(minNanos, maxNanos)
      unit <- timeUnitArb.arbitrary
    } yield Duration(length, NANOSECONDS).toUnitPrecise(unit)
  }

  implicit def arbDuration(implicit timeUnitArb: Arbitrary[TimeUnit]): Arbitrary[Duration] = Arbitrary {
    for {
      length <- Gen.oneOf(
        Gen.chooseNum(minNanos, maxNanos),
        Gen.oneOf(Double.NegativeInfinity, Double.MinPositiveValue, Double.PositiveInfinity, Double.NaN)
      )
      unit <- timeUnitArb.arbitrary
    } yield length match {
      case nanos: Long =>
        Duration(nanos, NANOSECONDS).toUnitPrecise(unit)
      case inf: Double =>
        Duration(inf, NANOSECONDS)
    }
  }

}

object DurationGenerators extends DurationGenerators

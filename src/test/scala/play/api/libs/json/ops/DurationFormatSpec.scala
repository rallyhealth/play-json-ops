package play.api.libs.json.ops

import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.scalacheck.DurationGenerators._
import play.api.libs.json.scalatest.PlayJsonFormatSpec

import scala.concurrent.duration._

class FiniteDurationArrayFormatSpec extends PlayJsonFormatSpec[FiniteDuration](
  arbitrary[FiniteDuration])(DurationFormat.array.finiteDurationFormat, implicitly, implicitly)
  with AssertDurationEquality[FiniteDuration]

class FiniteDurationStringFormatSpec extends PlayJsonFormatSpec[FiniteDuration](
  arbitrary[FiniteDuration])(DurationFormat.string.finiteDurationFormat, implicitly, implicitly)
  with AssertDurationEquality[FiniteDuration]

class DurationArrayFormatSpec extends PlayJsonFormatSpec[Duration](
  arbitrary[Duration])(DurationFormat.array.durationFormat, implicitly, implicitly)
  with AssertDurationEquality[Duration]

class DurationStringFormatSpec extends PlayJsonFormatSpec[Duration](
  arbitrary[FiniteDuration])(DurationFormat.string.durationFormat, implicitly, implicitly)
  with AssertDurationEquality[Duration]

private[ops] trait AssertDurationEquality[T <: Duration] extends PlayJsonFormatSpec[T] {

  override protected def assertPostSerializationEquality(expected: T, actual: T): Unit = {
    if (expected.isFinite()) {
      assert(actual.isFinite(), s"$actual is not finite and cannot be equal to $expected")
      assert(expected.unit == actual.unit)
      assert(expected.length == actual.length)
    }
    else if (expected eq Duration.Undefined) {
      assert(actual eq Duration.Undefined)
    }
    else {
      assert(!actual.isFinite(), s"$actual is finite and cannot be equal to $expected")
      assert(expected == actual)
    }
  }
}
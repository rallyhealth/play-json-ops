package play.api.libs.json.ops.v4

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
    if (expected.isFinite) {
      assert(actual.isFinite, s"$actual is not finite and cannot be equal to $expected")
      assertResult(expected.unit)(actual.unit)
      assertResult(expected.length)(actual.length)
    }
    else if (expected eq Duration.Undefined) {
      assertResult(Duration.Undefined)(actual)
    }
    else {
      assert(!actual.isFinite, s"$actual is finite and cannot be equal to $expected")
      assertResult(expected)(actual)
    }
  }
}

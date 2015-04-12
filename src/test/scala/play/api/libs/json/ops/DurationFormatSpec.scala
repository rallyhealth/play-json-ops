package play.api.libs.json.ops

import org.scalactic.Tolerance
import org.scalatest.FlatSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import play.api.libs.json.Json
import play.api.libs.json.scalacheck.DurationGenerators

import scala.concurrent.duration._

class DurationFormatSpec extends FlatSpec
with GeneratorDrivenPropertyChecks
with Tolerance
with JsonImplicits
with DurationGenerators {

  def assertAlmostEqual(expected: Duration, actual: Duration): Unit = {
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

  behavior of "DurationFormat.string"

  it should "read the Duration it writes" in {
    forAll() { (duration: Duration) =>
      import DurationFormat.string.format
      val json = Json.toJson(duration)
      val result = json.asOrThrow[Duration]
      assertAlmostEqual(result, duration)
    }
  }

  it should "read the FiniteDuration it writes as a Duration" in {
    forAll() { (duration: FiniteDuration) =>
      import DurationFormat.string.format
      val json = Json.toJson(duration)
      val result = json.asOrThrow[Duration]
      assertAlmostEqual(result, duration)
    }
  }

  behavior of "DurationFormat.array"

  it should "read the Duration it writes" in {
    forAll() { (duration: Duration) =>
      import DurationFormat.array.format
      val json = Json.toJson(duration)
      val result = json.asOrThrow[Duration]
      assertAlmostEqual(result, duration)
    }
  }

  it should "read the FiniteDuration it writes as a Duration" in {
    forAll() { (duration: FiniteDuration) =>
      import DurationFormat.array.format
      val json = Json.toJson(duration)
      val result = json.asOrThrow[Duration]
      assertAlmostEqual(result, duration)
    }
  }
}

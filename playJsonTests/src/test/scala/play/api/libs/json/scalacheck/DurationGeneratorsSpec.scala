package play.api.libs.json.scalacheck

import org.scalatest.FlatSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import scala.concurrent.duration.{Duration, FiniteDuration}

class DurationGeneratorsSpec extends FlatSpec with GeneratorDrivenPropertyChecks with DurationGenerators {

  "Arbitrary[FiniteDuration]" should "always produce a valid value" in {
    forAll() { (duration: FiniteDuration) =>
      assert(duration ne null)
    }
  }

  "Arbitrary[Duration]" should "always produce a valid value" in {
    forAll() { (duration: Duration) =>
      assert(duration ne null)
    }
  }
}

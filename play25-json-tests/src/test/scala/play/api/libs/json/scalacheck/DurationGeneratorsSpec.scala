package play.api.libs.json.scalacheck

import org.scalatest.WordSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import play.api.libs.json.scalacheck.DurationGenerators._

import scala.concurrent.duration.{Duration, FiniteDuration}

class DurationGeneratorsSpec extends WordSpec
  with GeneratorDrivenPropertyChecks {

  "Arbitrary[FiniteDuration]" should {
    "always produce a valid finite value" in {
      forAll() { (duration: FiniteDuration) =>
        assert(duration.isFinite())
      }
    }
  }

  "Arbitrary[Duration]" should {
    "always produce a valid value" in {
      forAll() { (duration: Duration) =>
        assert(duration ne null)
      }
    }
  }
}

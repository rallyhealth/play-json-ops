package play.api.libs.json.scalacheck

import org.scalacheck.Arbitrary
import org.scalatest.FlatSpecLike
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import play.api.libs.json.Json
import play.api.libs.json.scalatest.PlayJsonFormatSpec

import scala.language.implicitConversions

case class Example(value: String)

object Example {
  implicit val format = Json.format[Example]
}

trait PlayJsonExampleGenerators {

  implicit def arbExample(implicit arbString: Arbitrary[String]): Arbitrary[Example] =
    Arbitrary(arbString.arbitrary map { Example(_) })
}

object PlayJsonExampleGenerators extends PlayJsonExampleGenerators

import PlayJsonExampleGenerators._

/**
 * No additional tests
 */
class PlayJsonFormatSpecExample extends PlayJsonFormatSpec[Example]

/**
 * Additional [[FlatSpecLike]] tests
 */
class PlayJsonFormatFlatSpecExample extends PlayJsonFormatSpec[Example]
with GeneratorDrivenPropertyChecks {

  "PlayJsonFormatFlatSpecExample" should "allow adding additional specs" in {
    forAll() { (example: Example) =>
      assert(example.value ne null)
    }
  }
}

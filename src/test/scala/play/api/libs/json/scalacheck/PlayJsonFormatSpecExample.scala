package play.api.libs.json.scalacheck

import org.scalacheck.Shrink.shrink
import org.scalacheck.ops.ScalaCheckImplicits
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FlatSpecLike, Matchers}
import play.api.libs.json.Json
import play.api.libs.json.scalatest.PlayJsonFormatSpec

import scala.language.implicitConversions

case class Example(value: String, nested: Seq[Example])

object Example {
  implicit val format = Json.format[Example]
}

trait PlayJsonExampleGenerators {

  def genExample(depth: Int)(implicit arbString: Arbitrary[String]): Gen[Example] =
    for {
      str <- arbString.arbitrary
      nested <- Gen.listOfN(depth, genExample(depth - 1))
    } yield Example(str, nested)

  implicit def arbExample(implicit arbString: Arbitrary[String]): Arbitrary[Example] = Arbitrary {
    Gen.choose(0, 5) flatMap genExample
  }

  implicit val shrinkExample: Shrink[Example] = {
    val extract = Example.unapply _
    val rebuild = (Example.apply _).tupled
    Shrink {
      example =>
        val shrinkTupled = shrink(extract(example).get)
        val stream = shrinkTupled map rebuild
        stream
    }
  }
}

object PlayJsonExampleGenerators extends PlayJsonExampleGenerators

import play.api.libs.json.scalacheck.PlayJsonExampleGenerators._

/**
 * No additional tests
 */
class PlayJsonFormatSpecExample extends PlayJsonFormatSpec[Example]

/**
 * Additional [[FlatSpecLike]] tests
 */
class PlayJsonFormatFlatSpecExample extends PlayJsonFormatSpec[Example]
with FlatSpecLike
with Matchers
with ScalaCheckImplicits
with GeneratorDrivenPropertyChecks {

  "PlayJsonFormatFlatSpecExample" should "allow adding additional specs" in {
    forAll() { (example: Example) =>
      assert(Json.toJson(example).as[Example] == example)
    }
  }

  "PlayJsonFormatFlatSpecExample.shrink" should "use the implicit shrink" in {
    val example = Arbitrary.arbitrary[Example].suchThat(_.nested.nonEmpty).getOrThrow
    val ex = intercept[TestFailedException] {
      val expected = example.copy(nested = Seq())
      assertSameWithShrink(expected, example, Json.toJson(example))
    }
    assert(ex.getSuppressed.head.getMessage contains "shrink")
  }
}

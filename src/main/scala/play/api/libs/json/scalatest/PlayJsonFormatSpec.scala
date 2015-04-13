package play.api.libs.json.scalatest

import org.scalacheck.ops._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.FlatSpecLike
import play.api.libs.json.Format
import play.api.libs.json.scalacheck.PlayJsonFormatTests

import scala.reflect.ClassTag
import scala.testing.scalatest.ScalaTestBridge

/**
 * Extend this for free serialization tests given a [[Format]] and some example values.
 *
 * @note this class extends from [[FlatSpecLike]], so any additional tests you provide must be
 *       in that flavor of testing.
 *
 * Import an [[Arbitrary]] of your model to have even better test coverage, as ScalaCheck will
 * trace the edge cases of serialization for you.
 */
class PlayJsonFormatSpec[T](examples: Seq[T])(implicit playFormat: Format[T], clsTag: ClassTag[T])
  extends PlayJsonFormatTests[T](examples)
  with FlatSpecLike
  with ScalaTestBridge {

  def this(gen: Gen[T], samples: Int)(implicit playFormat: Format[T], clsTag: ClassTag[T]) =
    this(gen.toIterator.take(samples).toSeq)

  def this(gen: Gen[T])(implicit playFormat: Format[T], clsTag: ClassTag[T]) = this(gen, 100)

  def this(samples: Int)(implicit arb: Arbitrary[T], playFormat: Format[T], clsTag: ClassTag[T]) =
    this(arb.arbitrary, samples)

  def this()(implicit arb: Arbitrary[T], playFormat: Format[T], clsTag: ClassTag[T]) = this(arb.arbitrary)
}

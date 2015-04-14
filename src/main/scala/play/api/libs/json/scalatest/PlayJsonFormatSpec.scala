package play.api.libs.json.scalatest

import org.scalacheck.ops._
import org.scalacheck.{Shrink, Arbitrary, Gen}
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
class PlayJsonFormatSpec[T](examples: Seq[T])(implicit playFormat: Format[T], clsTag: ClassTag[T], shrink: Shrink[T])
  extends PlayJsonFormatTests[T](examples, playFormat, clsTag, shrink)
  with FlatSpecLike
  with ScalaTestBridge {

  def this(gen: Gen[T], samples: Int)(implicit playFormat: Format[T], clsTag: ClassTag[T], shrink: Shrink[T]) =
    this(gen.toIterator.take(samples).toSeq)

  def this(gen: Gen[T])(implicit playFormat: Format[T], clsTag: ClassTag[T], shrink: Shrink[T]) = this(gen, 100)

  def this(samples: Int)(implicit playFormat: Format[T], clsTag: ClassTag[T], shrink: Shrink[T], arb: Arbitrary[T]) =
    this(arb.arbitrary, samples)

  def this()(implicit playFormat: Format[T], clsTag: ClassTag[T], shrink: Shrink[T], arb: Arbitrary[T]) =
    this(arb.arbitrary)
}

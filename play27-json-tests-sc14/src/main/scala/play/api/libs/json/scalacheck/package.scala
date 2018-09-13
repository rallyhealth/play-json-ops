package play.api.libs.json

import scala.language.higherKinds

package object scalacheck {

  /**
    * An extension class that is used when parallel collections are not available for this Scala version.
    *
    * This is needed because the parallel collections module has not been released for Scala 2.13.0-M4.
    */
  private[scalacheck] implicit class VoidParallelCollectionOps[C[_] <: Iterable[_], T](private val col: C[T]) extends AnyVal {

    /**
      * Returns the original collection without parallel collection support.
      */
    def par: C[T] = col
  }

}

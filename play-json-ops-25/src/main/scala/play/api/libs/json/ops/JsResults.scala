package play.api.libs.json.ops

import play.api.libs.json.{JsError, JsPath, JsResult}

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

/**
 * Useful helper methods for combining and filtering [[JsResult]]s.
 */
object JsResults {

  /**
   * Filter out all [[JsError]]s and return a collection of results with the most specific type.
   *
   * @param results the results to filter
   * @return a collection of results in whatever order the given collection type maintains
   */
  def ignoreErrors[T, C[_]](results: Traversable[JsResult[T]])
    (implicit bf: CanBuildFrom[Traversable[JsResult[T]], T, C[T]]): C[T] = {
    results.filter(_.isSuccess).map(_.get)
  }

  /**
   * Collects all [[JsError]]s and appends the index of result to path of the error.
   *
   * @param results a sequence of results
   * @return a collection of the JsError objects in the sequence that they were given
   */
  def collectErrors[C[_]](results: Seq[JsResult[_]])
    (implicit bf: CanBuildFrom[Seq[JsResult[_]], JsError, C[JsError]]): C[JsError] = {
    results.zipWithIndex map {
      case (result, index) => result.repath(JsPath(index))
    } collect {
      case error: JsError => error
    }
  }

  /**
   * Groups all [[JsError.errors]] by key and concatenates errors with the same key.
   *
   * @see [[JsError.merge]]
   * @param errors the errors to merge
   * @return all the errors merged by key or None if the given errors are empty
   */
  def flattenErrors(errors: Traversable[JsError]): Option[JsError] = {
    errors.reduceOption[JsError] {
      case (e1, e2) => e1 ++ e2
    }
  }

  /**
   * Collects all [[JsError]]s and appends the index of the result to the path of the error,
   * and then flattens all the errors into a single [[JsError]].
   *
   * This is effectively the same as:
   * {{{
   *   JsResults.flattenErrors(JsResults.collectErrors(results))
   * }}}
   *
   * @note the paths of the results will never overlap, so the merge will never concatenate
   *       errors from different indexes in the sequence of results
   *
   * @param results a sequence of [[JsResult]]s
   * @return all the errors merged
   */
  def collectFlatError(results: Seq[JsResult[_]]): Option[JsError] = {
    flattenErrors(collectErrors(results))
  }
}

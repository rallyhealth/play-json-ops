package play.api.libs.json.ops

import play.api.libs.json._

import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.util.control.NonFatal

trait RecoverOps[F[x] <: Reads[x], A] extends Any {

  def unsafeReader: Reads[A]

  /**
   * Recovers from all exceptions thrown during reading, producing an exception-safe Reads.
   */
  def recoverTotal(recoverFn: Throwable => A): Reads[A] = build {
    Reads { json: JsValue =>
      try {
        unsafeReader.reads(json)
      } catch {
        case NonFatal(ex) => JsSuccess(recoverFn(ex))
      }
    }
  }

  /**
   * Translates exceptions thrown during reading into JsError validation results.
   */
  def recoverJsError(implicit ct: ClassTag[A]): Reads[A] = {

    /**
     * Following the style of play json's DefaultReads class. Type should be all lowercase.
     * e.g.
     * error.expected.string
     * error.expected.uuid
     */
    val className =
      ct.runtimeClass.getSimpleName.toLowerCase.takeWhile(_ != '$')
    val errorKey = s"error.expected.$className"
    recoverWith {
      case ex => JsError(JsonValidationError(errorKey, ex))
    }
  }

  /**
   * Recovers from some exceptions thrown during reading into a [[JsResult]].
   *
   * @note if the recover function is undefined for an exception, the [[Reads]] produced is still unsafe.
   */
  def recoverWith(
    recoverFn: PartialFunction[Throwable, JsResult[A]]
  ): Reads[A] = build {
    Reads { json: JsValue =>
      try {
        unsafeReader.reads(json)
      } catch {
        case NonFatal(ex) if recoverFn isDefinedAt ex => recoverFn(ex)
      }
    }
  }

  // Subclasses need to define how to build an instance of F[A] from a Reads[A]
  protected def build(safeReader: Reads[A]): F[A]
}

class ReadsRecoverOps[A](override val unsafeReader: Reads[A]) extends AnyVal with RecoverOps[Reads, A] {
  final override protected def build(safeReader: Reads[A]): Reads[A] = safeReader
}

class FormatRecoverOps[A](val unsafeFormat: Format[A]) extends AnyVal with RecoverOps[Format, A] {
  final override def unsafeReader: Reads[A] = unsafeFormat
  final override protected def build(safeReader: Reads[A]): Format[A] = Format(safeReader, unsafeFormat)
}

class OFormatRecoverOps[A](val unsafeFormat: OFormat[A]) extends AnyVal with RecoverOps[OFormat, A] {
  final override def unsafeReader: Reads[A] = unsafeFormat
  final override protected def build(safeReader: Reads[A]): OFormat[A] = OFormat(safeReader, unsafeFormat)
}

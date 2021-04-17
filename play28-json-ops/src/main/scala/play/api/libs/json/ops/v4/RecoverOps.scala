package play.api.libs.json.ops.v4

import play.api.libs.json._

import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.util.control.NonFatal

sealed trait RecoverOps[F[x] <: Reads[x], A] extends Any {

  def unsafeReader: Reads[A]

  /**
   * Recovers from all exceptions thrown during reading, producing an exception-safe Reads.
   */
  def recoverTotal(recoverFn: Throwable => A): Reads[A] = build { json =>
    try {
      unsafeReader.reads(json)
    } catch {
      case NonFatal(ex) => JsSuccess(recoverFn(ex))
    }
  }

  /**
   * Translates exceptions thrown during reading into JsError validation results.
   */
  def recoverJsError(implicit ct: ClassTag[A]): Reads[A] = {
    recoverWith {
      case ex => RecoverOps.expectedTypeError(ct.runtimeClass, ex)
    }
  }

  /**
   * Recovers from some exceptions thrown during reading into a [[JsResult]].
   *
   * @note if the recover function is undefined for an exception, the [[Reads]] produced is still unsafe.
   */
  def recoverWith(
    recoverFn: PartialFunction[Throwable, JsResult[A]]
  ): Reads[A] = build { json =>
    try {
      unsafeReader.reads(json)
    } catch {
      case NonFatal(ex) if recoverFn isDefinedAt ex => recoverFn(ex)
    }
  }

  // Subclasses need to define how to build an instance of F[A] from a Reads[A]
  protected def build(safeReader: Reads[A]): F[A]
}

object RecoverOps {

  /**
   * Similar to the Class.getSimpleName method, except it does not throw any exceptions and
   * handles Scala inner classes better.
   */
  private def safeSimpleClassName(cls: Class[_]): String = {
    ClassNameUtils.safeSimpleClassName(cls)
  }

  /**
   * Following the style of play json's DefaultReads class. Type should be all lowercase.
   *
   * @note this method is not cross-compatible between Play 2.5 and above. Once everything
   *       uses [[JsonValidationError]], we can move this whole file to the common project.
   *
   * e.g.
   * error.expected.string
   * error.expected.uuid
   */
  private def expectedTypeError(tpe: String, args: Any*): JsError = {
    val className = tpe.toLowerCase
    JsError(JsonValidationError(s"error.expected.$className", args: _*))
  }

  /**
   * Same as [[expectedTypeError]], except safely converts the class to a string.
   */
  private def expectedTypeError(cls: Class[_], args: Any*): JsError = {
    expectedTypeError(safeSimpleClassName(cls), args: _*)
  }
}

final class ReadsRecoverOps[A](override val unsafeReader: Reads[A]) extends AnyVal with RecoverOps[Reads, A] {
  override protected def build(safeReader: Reads[A]): Reads[A] = safeReader
}

final class FormatRecoverOps[A](val unsafeFormat: Format[A]) extends AnyVal with RecoverOps[Format, A] {
  override def unsafeReader: Reads[A] = unsafeFormat
  override protected def build(safeReader: Reads[A]): Format[A] = Format(safeReader, unsafeFormat)
}

final class OFormatRecoverOps[A](val unsafeFormat: OFormat[A]) extends AnyVal with RecoverOps[OFormat, A] {
  override def unsafeReader: Reads[A] = unsafeFormat
  override protected def build(safeReader: Reads[A]): OFormat[A] = OFormat(safeReader, unsafeFormat)
}

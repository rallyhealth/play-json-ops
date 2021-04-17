package play.api.libs.json.ops.v4

import play.api.libs.json.{JsPath, Reads}

import scala.collection.{BuildFrom, Factory}
import scala.language.{higherKinds, implicitConversions}

/**
 * Provides helpers which will create empty containers if a json field is missing
 *
 * @see [[play.api.libs.json.ops.TolerantContainerPath.readNullableContainer]]
 */
trait TolerantContainerFormats {
  implicit final def getTolerantContainerPath(jsPath: JsPath): TolerantContainerPath = new TolerantContainerPath(jsPath)
}

class TolerantContainerPath(val jsPath: JsPath) extends AnyVal {

  /**
   * Defines a [[Reads]] that will read all container fields such that `null=[]` (or an empty [[Set]] / [[Map]])
   */
  def readNullableContainer[C[_], A](implicit r: Reads[C[A]], f: Factory[A, C[A]]): Reads[C[A]] = {
    Reads.nullable[C[A]](jsPath)(r).map(_.getOrElse(f.newBuilder.result()))
  }
}

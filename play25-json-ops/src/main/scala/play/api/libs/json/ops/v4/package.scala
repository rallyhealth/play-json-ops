package play.api.libs.json.ops

import play.api.libs.json.{Format, OFormat, Reads}

import scala.language.implicitConversions

package object v4 extends JsonImplicits {

  implicit def safeReadsOps[A](reads: Reads[A]): ReadsRecoverOps[A] = new ReadsRecoverOps(reads)

  implicit def safeFormatOps[A](format: Format[A]): FormatRecoverOps[A] = new FormatRecoverOps(format)

  implicit def safeOFormatOps[A](oformat: OFormat[A]): OFormatRecoverOps[A] = new OFormatRecoverOps(oformat)
}

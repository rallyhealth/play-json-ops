package play.api.libs.json

import scala.language.implicitConversions

package object ops extends JsonImplicits {

  implicit def safeReadsOps[A](reads: Reads[A]): ReadsRecoverOps[A] = new ReadsRecoverOps(reads)

  implicit def safeFormatOps[A](format: Format[A]): FormatRecoverOps[A] = new FormatRecoverOps(format)

  implicit def safeOFormatOps[A](oformat: OFormat[A]): OFormatRecoverOps[A] = new OFormatRecoverOps(oformat)
}

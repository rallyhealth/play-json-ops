package play.api.libs.json.ops

import play.api.libs.json.OFormat

object OFormatOps {

  def of[T: OFormat]: OFormat[T] = implicitly
}

trait OFormatOps {
}

package play.api.libs.json.ops

import play.api.libs.json._

import scala.concurrent.duration._
import scala.util.control.NonFatal

/**
 * Provides an implicit reader for Scala [[Duration]]s.
 */
trait ImplicitDurationReads {

  implicit protected def durationReads: Reads[Duration]
}

/**
 * Provides an implicit writer for Scala [[Duration]]s.
 */
trait ImplicitDurationWrites {

  implicit protected def durationWrites: Writes[Duration]
}

/**
 * Reads and writes a Scala [[Duration]] as a flat string.
 *
 * For example: Duration("1 second") => "1 second"
 */
trait StringDurationFormat extends ImplicitDurationReads with ImplicitDurationWrites {

  override implicit protected val durationReads: Reads[Duration] = new Reads[Duration] {
    override def reads(json: JsValue): JsResult[Duration] = json.validate[String] flatMap { str =>
      try JsSuccess(Duration(str))
      catch {
        case NonFatal(ex: NumberFormatException) => JsError(ex.toString)
      }
    }
  }

  override implicit protected val durationWrites: Writes[Duration] = new Writes[Duration] {
    override def writes(o: Duration): JsValue = JsString(o.toString)
  }
}

/**
 * Reads and writes a Scala [[Duration]] as an array with the number and the [[TimeUnit]] as a string.
 *
 * For example: Duration("1 second") => [1, "SECONDS"]
 */
trait ArrayDurationFormat extends ImplicitDurationReads with ImplicitDurationWrites with ImplicitTupleFormats {

  override implicit protected val durationReads: Reads[Duration] = new Reads[Duration] {
    override def reads(json: JsValue): JsResult[Duration] = json.validate[(Long, String)] flatMap {
      case (x, unit) =>
        try JsSuccess(Duration(x, unit))
        catch {
          case NonFatal(ex: NumberFormatException) => JsError(ex.toString)
        }
    }
  }

  override implicit protected val durationWrites: Writes[Duration] = new Writes[Duration] {
    override def writes(o: Duration): JsValue = JsArray(Seq(JsNumber(o.length), JsString(o.unit.toString)))
  }
}

/**
 * Reads a Scala [[Duration]] as either a string or array as written by [[StringDurationFormat]]
 * or [[ArrayDurationFormat]].
 *
 * For example:
 *   "1 seconds"    => Duration("1 seconds")
 *   [1, "SECONDS"] => Duration(1, "SECONDS")
 */
trait ForgivingDurationReads extends ImplicitDurationReads {

  override implicit protected val durationReads: Reads[Duration] = {
    DurationFormat.string.format orElse DurationFormat.array.format
  }
}

/**
 * The enumeration of the various formats for [[Duration]].
 */
object DurationFormat {

  object string extends StringDurationFormat {
    implicit val format: Format[Duration] = Format(durationReads, durationWrites)
  }

  object array extends ArrayDurationFormat {
    implicit val format: Format[Duration] = Format(durationReads, durationWrites)
  }
}

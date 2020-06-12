package play.api.libs.json.ops.v4

import play.api.libs.json._

import scala.concurrent.duration._
import scala.concurrent.duration.ops.v4.DurationOps
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/**
 * Provides implicit readers for Scala [[Duration]]s.
 */
trait ImplicitDurationReads {

  implicit protected def finiteDurationReads: Reads[FiniteDuration]

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

  override implicit val finiteDurationReads: Reads[FiniteDuration] = Reads {
    _.validate[String] flatMap { str =>
      try {
        DurationOps.parseLossless(str) match {
          case Success(finite: FiniteDuration) => JsSuccess(finite)
          case _ => JsError("error.expected.duration.finite")
        }
      }
      catch {
        case NonFatal(ex: NumberFormatException) => JsError(ex.getMessage)
      }
    }
  }

  override implicit val durationReads: Reads[Duration] = Reads {
    _.validate[String] flatMap { str =>
      DurationOps.parseLossless(str) match {
        case Success(duration) =>
          JsSuccess(duration)
        case Failure(ex) =>
          JsError(s"error.expected.duration (${ex.getMessage})")
      }
    }
  }

  override implicit val durationWrites: Writes[Duration] = Writes { duration =>
    JsString(DurationFormat.asString(duration))
  }
}

/**
 * Reads and writes a Scala [[Duration]] as an array with the number and the [[TimeUnit]] as a string.
 *
 * For example: Duration("1 second") => [1, "SECONDS"]
 */
trait ArrayDurationFormat extends ImplicitDurationReads with ImplicitDurationWrites {

  override implicit val finiteDurationReads: Reads[FiniteDuration] = Reads {
    _.validate[(Long, String)] flatMap {
      case (length, unit) =>
        try JsSuccess(Duration(length, unit.toLowerCase))
        catch {
          case NonFatal(ex: NumberFormatException) => JsError(ex.getMessage)
        }
    }
  }

  override implicit val durationReads: Reads[Duration] = Reads { json =>
    json.asOpt[String] match {
      case Some(str) =>
        DurationOps.parseLossless(str) match {
          case Success(duration) => JsSuccess(duration)
          case Failure(ex) => JsError(s"error.expected.duration (${ex.getMessage})")
        }
      case None =>
        finiteDurationReads reads json
    }
  }

  override implicit val durationWrites: Writes[Duration] = Writes { duration =>
    if (duration.isFinite)
      JsArray(Seq(JsNumber(duration.length), JsString(duration.unit.toString)))
    else // strip off the Duration prefix and serialize as string
      JsString(DurationFormat.asString(duration))
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

  override implicit val durationReads: Reads[Duration] = {
    DurationFormat.string.durationFormat orElse DurationFormat.array.durationFormat
  }
}

/**
 * The enumeration of the various formats for [[Duration]].
 */
object DurationFormat {

  def asString(duration: Duration): String = duration match {
    case inf: Duration.Infinite => asString(inf)
    case finite => finite.toString
  }

  def asString(inf: Duration.Infinite): String = inf.toString.substring("Duration.".length)

  object string extends StringDurationFormat {
    implicit val durationFormat: Format[Duration] = Format(durationReads, durationWrites)
    implicit val finiteDurationFormat: Format[FiniteDuration] = Format(finiteDurationReads, durationWrites)
  }

  object array extends ArrayDurationFormat {
    implicit val durationFormat: Format[Duration] = Format(durationReads, durationWrites)
    implicit val finiteDurationFormat: Format[FiniteDuration] = Format(finiteDurationReads, durationWrites)
  }
}

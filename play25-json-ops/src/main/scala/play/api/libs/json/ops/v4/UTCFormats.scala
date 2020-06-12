package play.api.libs.json.ops.v4

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json._

/**
 * Provides a [[Format]] for [[DateTime]] that always reads and writes in UTC.
 *
 * @note this only applies for [[DateTime]] because [[org.joda.time.LocalDateTime]],
 *       [[java.util.Date]], and [[java.sql.Date]] do not carry along the time zone.
 */
@deprecated(
  "A better approach is to explicitly convert time zones in your own code when appropriate and use the timezone " +
    "passed in the JSON string or fallback on one that is configured for the user.  " +
    "If no timezone is given in the JSON, the default will be the global DateTimeZone.getDefault (which is mutable).  " +
    "This is not available for Play >2.5 artifacts since 2.0.0 and will be removed when Play 2.5 support is removed.",
  "3.1.0"
)
trait UTCFormats {

  /**
   * For when you don't care about the [[DateTimeZone]] of the server
   * that is parsing the [[DateTime]] and prefer to have all dates and times in
   * Universal Coordinated Time (UTC).
   */
  implicit object ReadsDateTimeUTC extends Format[DateTime] {
    override def reads(json: JsValue): JsResult[DateTime] =
      Reads.DefaultJodaDateReads.reads(json).map(_ withZone DateTimeZone.UTC)
    override def writes(dt: DateTime): JsValue =
      Writes.DefaultJodaDateWrites.writes(dt)  // as JsNumber of millis since epoch
  }
}

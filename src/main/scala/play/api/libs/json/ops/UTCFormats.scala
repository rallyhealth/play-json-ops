package play.api.libs.json.ops

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json._

import scala.language.implicitConversions

/**
 * Provides a [[Format]] for [[DateTime]] that always reads and writes in UTC.
 *
 * @note this only applies for [[DateTime]] because [[org.joda.time.LocalDateTime]],
 *       [[java.util.Date]], and [[java.sql.Date]] do not carry along the time zone.
 */
trait UTCFormats {

  /**
   * A good default for when you don't care about the [[DateTimeZone]] of the server
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

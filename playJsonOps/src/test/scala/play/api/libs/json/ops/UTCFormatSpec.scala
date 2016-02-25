package play.api.libs.json.ops

import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.WordSpec
import play.api.libs.json.{Format, Json}

case class NotUTC(when: DateTime)
object NotUTC {
  implicit val format: Format[NotUTC] = Json.format[NotUTC]
}

case class UseUTC(when: DateTime)
object UseUTC extends UTCFormats {
  implicit val format: Format[UseUTC] = Json.format[UseUTC]
}

class UTCFormatSpec extends WordSpec {

  "Json.format by default" should {
    "deserialize with the current time zone" in {
      val dt = new DateTime
      assertResult(dt.getZone) {
        val notUTC = Json.toJson(NotUTC(dt)).as[NotUTC]
        notUTC.when.getZone
      }
    }
  }

  "UTCFormats" should {

    "override the standard Format[DateTime]" in {
      val dt = new DateTime
      assertResult(DateTimeZone.UTC) {
        val useUTC = Json.toJson(UseUTC(dt)).as[UseUTC]
        useUTC.when.getZone
      }
    }
  }
}


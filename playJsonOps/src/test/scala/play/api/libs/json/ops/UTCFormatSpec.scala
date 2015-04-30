package play.api.libs.json.ops

import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.FlatSpec
import play.api.libs.json.{Format, Json}

object NotUTC {

  implicit val format: Format[NotUTC] = Json.format[NotUTC]
}

case class NotUTC(when: DateTime)

object UseUTC extends UTCFormats {

  implicit val format: Format[UseUTC] = Json.format[UseUTC]
}

case class UseUTC(when: DateTime)

class UTCFormatSpec extends FlatSpec {

  "the standard Format[DateTime]" should "deserialize with the current time zone" in {
    val dt = new DateTime
    val notUTC = Json.toJson(NotUTC(dt)).as[NotUTC]
    assert(notUTC.when.getZone == dt.getZone)
  }

  "extending UTCFormat" must "override the standard Format[DateTime]" in {
    val dt = new DateTime
    val useUTC = Json.toJson(UseUTC(dt)).as[UseUTC]
    assert(useUTC.when.getZone == DateTimeZone.UTC)
  }
}


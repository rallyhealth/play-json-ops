package play.api.libs.json.ops

import play.api.libs.json._
import scala.language.implicitConversions

object TupleFormats extends ImplicitTupleFormats

/**
 * Extend this to have implicit [[Reads]] and [[Writes]] for tuples.
 *
 * The format for these tuples is to use JsArray.
 *
 * {{{
 *   Json.parse("""[1, "2", true]""").as[(Int, String, Boolean)]  // will work
 * }}}
 */
trait ImplicitTupleFormats {

  implicit def tuple2Reads[A: Reads, B: Reads]: Reads[(A, B)] =
    new Reads[(A, B)] {
      override def reads(json: JsValue): JsResult[(A, B)] = json.validate[Seq[JsValue]] flatMap {
        case Seq(_1, _2) =>
          val a = _1.validate[A]
          val b = _2.validate[B]
          val validations = Seq(a, b)
          if (validations.forall(_.isSuccess)) JsSuccess((a.get, b.get))
          else JsResults.collectFlatError(validations).get
        case _ => JsError("error.expected.jsarray.size.2")
      }
    }

  implicit def tuple2Writes[A: Writes, B: Writes]: Writes[(A, B)] =
    new Writes[(A, B)] {
      override def writes(o: (A, B)): JsValue = JsArray(Seq(
        Json.toJson(o._1),
        Json.toJson(o._2)
      ))
    }

  implicit def tuple3Reads[A: Reads, B: Reads, C: Reads]: Reads[(A, B, C)] =
    new Reads[(A, B, C)] {
      override def reads(json: JsValue): JsResult[(A, B, C)] = json.validate[Seq[JsValue]] flatMap {
        case Seq(_1, _2, _3) =>
          val a = _1.validate[A]
          val b = _2.validate[B]
          val c = _3.validate[C]
          val validations = Seq(a, b, c)
          if (validations.forall(_.isSuccess)) JsSuccess((a.get, b.get, c.get))
          else JsResults.collectFlatError(validations).get
        case _ => JsError("error.expected.jsarray.size.3")
      }
    }

  implicit def tuple3Writes[A: Writes, B: Writes, C: Writes]: Writes[(A, B, C)] =
    new Writes[(A, B, C)] {
      override def writes(o: (A, B, C)): JsValue = JsArray(Seq(
        Json.toJson(o._1),
        Json.toJson(o._2),
        Json.toJson(o._3)
      ))
    }

  implicit def tuple4Reads[A: Reads, B: Reads, C: Reads, D: Reads]: Reads[(A, B, C, D)] =
    new Reads[(A, B, C, D)] {
      override def reads(json: JsValue): JsResult[(A, B, C, D)] = json.validate[Seq[JsValue]] flatMap {
        case Seq(_1, _2, _3, _4) =>
          val a = _1.validate[A]
          val b = _2.validate[B]
          val c = _3.validate[C]
          val d = _4.validate[D]
          val validations = Seq(a, b, c, d)
          if (validations.forall(_.isSuccess)) JsSuccess((a.get, b.get, c.get, d.get))
          else JsResults.collectFlatError(validations).get
        case _ => JsError("error.expected.jsarray.size.4")
      }
    }

  implicit def tuple4Writes[A: Writes, B: Writes, C: Writes, D: Writes]: Writes[(A, B, C, D)] =
    new Writes[(A, B, C, D)] {
      override def writes(o: (A, B, C, D)): JsValue = JsArray(Seq(
        Json.toJson(o._1),
        Json.toJson(o._2),
        Json.toJson(o._3),
        Json.toJson(o._4)
      ))
    }

  implicit def tuple5Reads[A: Reads, B: Reads, C: Reads, D: Reads, E: Reads]: Reads[(A, B, C, D, E)] =
    new Reads[(A, B, C, D, E)] {
      override def reads(json: JsValue): JsResult[(A, B, C, D, E)] = json.validate[Seq[JsValue]] flatMap {
        case Seq(_1, _2, _3, _4, _5) =>
          val a = _1.validate[A]
          val b = _2.validate[B]
          val c = _3.validate[C]
          val d = _4.validate[D]
          val e = _5.validate[E]
          val validations = Seq(a, b, c, d)
          if (validations.forall(_.isSuccess)) JsSuccess((a.get, b.get, c.get, d.get, e.get))
          else JsResults.collectFlatError(validations).get
        case _ => JsError("error.expected.jsarray.size.5")
      }
    }

  implicit def tuple5Writes[A: Writes, B: Writes, C: Writes, D: Writes, E: Writes]: Writes[(A, B, C, D, E)] =
    new Writes[(A, B, C, D, E)] {
      override def writes(o: (A, B, C, D, E)): JsValue = JsArray(Seq(
        Json.toJson(o._1),
        Json.toJson(o._2),
        Json.toJson(o._3),
        Json.toJson(o._4),
        Json.toJson(o._5)
      ))
    }
}

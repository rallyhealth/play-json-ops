package play.api.libs.json.ops

import play.api.libs.json._

import scala.language.implicitConversions

/**
  * Provides implicit [[Reads]] and [[Writes]] for tuples.
  *
  * The format for these tuples is to use JsArray.
  *
  * {{{
  *   import play.api.libs.json.ops._
  *
  *   Json.parse("""[1, "2", true]""").as[(Int, String, Boolean)]  // will work
  * }}}
  */
private[ops] object ImplicitTupleFormats extends ImplicitTupleFormats
private[ops] class ImplicitTupleFormats private[ops] {

  implicit def tuple2Reads[A: Reads, B: Reads]: Reads[(A, B)] =
    new Reads[(A, B)] {
      override def reads(json: JsValue): JsResult[(A, B)] = json.validate[Seq[JsValue]] flatMap {
        case Seq(_1, _2) =>
          val a = _1.validate[A]
          val b = _2.validate[B]
          val validations = Seq(a, b)
          if (validations.forall(_.isSuccess))
            JsSuccess((a.get, b.get))
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
          if (validations.forall(_.isSuccess))
            JsSuccess((a.get, b.get, c.get))
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
          if (validations.forall(_.isSuccess))
            JsSuccess((a.get, b.get, c.get, d.get))
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
          val validations = Seq(a, b, c, d, e)
          if (validations.forall(_.isSuccess))
            JsSuccess((a.get, b.get, c.get, d.get, e.get))
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

  implicit def tuple6Reads[
  A: Reads, B: Reads, C: Reads, D: Reads, E: Reads,
  F: Reads]: Reads[(A, B, C, D, E, F)] =
    new Reads[(A, B, C, D, E, F)] {
      override def reads(json: JsValue): JsResult[(A, B, C, D, E, F)] = json.validate[Seq[JsValue]] flatMap {
        case Seq(_1, _2, _3, _4, _5, _6) =>
          val a = _1.validate[A]
          val b = _2.validate[B]
          val c = _3.validate[C]
          val d = _4.validate[D]
          val e = _5.validate[E]
          val f = _6.validate[F]
          val validations = Seq(a, b, c, d, e, f)
          if (validations.forall(_.isSuccess))
            JsSuccess((a.get, b.get, c.get, d.get, e.get, f.get))
          else JsResults.collectFlatError(validations).get
        case _ => JsError("error.expected.jsarray.size.6")
      }
    }

  implicit def tuple6Writes[
  A: Writes, B: Writes, C: Writes, D: Writes, E: Writes,
  F: Writes]: Writes[(A, B, C, D, E, F)] =
    new Writes[(A, B, C, D, E, F)] {
      override def writes(o: (A, B, C, D, E, F)): JsValue = JsArray(Seq(
        Json.toJson(o._1),
        Json.toJson(o._2),
        Json.toJson(o._3),
        Json.toJson(o._4),
        Json.toJson(o._5),
        Json.toJson(o._6)
      ))
    }

  implicit def tuple7Reads[
  A: Reads, B: Reads, C: Reads, D: Reads, E: Reads,
  F: Reads, G: Reads]: Reads[(A, B, C, D, E, F, G)] =
    new Reads[(A, B, C, D, E, F, G)] {
      override def reads(json: JsValue): JsResult[(A, B, C, D, E, F, G)] = json.validate[Seq[JsValue]] flatMap {
        case Seq(_1, _2, _3, _4, _5, _6, _7) =>
          val a = _1.validate[A]
          val b = _2.validate[B]
          val c = _3.validate[C]
          val d = _4.validate[D]
          val e = _5.validate[E]
          val f = _6.validate[F]
          val g = _7.validate[G]
          val validations = Seq(a, b, c, d, e, f, g)
          if (validations.forall(_.isSuccess))
            JsSuccess((a.get, b.get, c.get, d.get, e.get, f.get, g.get))
          else JsResults.collectFlatError(validations).get
        case _ => JsError("error.expected.jsarray.size.7")
      }
    }

  implicit def tuple7Writes[
  A: Writes, B: Writes, C: Writes, D: Writes, E: Writes,
  F: Writes, G: Writes]: Writes[(A, B, C, D, E, F, G)] =
    new Writes[(A, B, C, D, E, F, G)] {
      override def writes(o: (A, B, C, D, E, F, G)): JsValue = JsArray(Seq(
        Json.toJson(o._1),
        Json.toJson(o._2),
        Json.toJson(o._3),
        Json.toJson(o._4),
        Json.toJson(o._5),
        Json.toJson(o._6),
        Json.toJson(o._7)
      ))
    }

  implicit def tuple8Reads[
  A: Reads, B: Reads, C: Reads, D: Reads, E: Reads,
  F: Reads, G: Reads, H: Reads]: Reads[(A, B, C, D, E, F, G, H)] =
    new Reads[(A, B, C, D, E, F, G, H)] {
      override def reads(json: JsValue): JsResult[(A, B, C, D, E, F, G, H)] = json.validate[Seq[JsValue]] flatMap {
        case Seq(_1, _2, _3, _4, _5, _6, _7, _8) =>
          val a = _1.validate[A]
          val b = _2.validate[B]
          val c = _3.validate[C]
          val d = _4.validate[D]
          val e = _5.validate[E]
          val f = _6.validate[F]
          val g = _7.validate[G]
          val h = _8.validate[H]
          val validations = Seq(a, b, c, d, e, f, g, h)
          if (validations.forall(_.isSuccess))
            JsSuccess((a.get, b.get, c.get, d.get, e.get, f.get, g.get, h.get))
          else JsResults.collectFlatError(validations).get
        case _ => JsError("error.expected.jsarray.size.8")
      }
    }

  implicit def tuple8Writes[
  A: Writes, B: Writes, C: Writes, D: Writes, E: Writes,
  F: Writes, G: Writes, H: Writes]: Writes[(A, B, C, D, E, F, G, H)] =
    new Writes[(A, B, C, D, E, F, G, H)] {
      override def writes(o: (A, B, C, D, E, F, G, H)): JsValue = JsArray(Seq(
        Json.toJson(o._1),
        Json.toJson(o._2),
        Json.toJson(o._3),
        Json.toJson(o._4),
        Json.toJson(o._5),
        Json.toJson(o._6),
        Json.toJson(o._7),
        Json.toJson(o._8)
      ))
    }

  implicit def tuple9Reads[
  A: Reads, B: Reads, C: Reads, D: Reads, E: Reads,
  F: Reads, G: Reads, H: Reads, I: Reads]: Reads[(A, B, C, D, E, F, G, H, I)] =
    new Reads[(A, B, C, D, E, F, G, H, I)] {
      override def reads(json: JsValue): JsResult[(A, B, C, D, E, F, G, H, I)] = json.validate[Seq[JsValue]] flatMap {
        case Seq(_1, _2, _3, _4, _5, _6, _7, _8, _9) =>
          val a = _1.validate[A]
          val b = _2.validate[B]
          val c = _3.validate[C]
          val d = _4.validate[D]
          val e = _5.validate[E]
          val f = _6.validate[F]
          val g = _7.validate[G]
          val h = _8.validate[H]
          val i = _9.validate[I]
          val validations = Seq(a, b, c, d, e, f, g, h, i)
          if (validations.forall(_.isSuccess))
            JsSuccess((a.get, b.get, c.get, d.get, e.get, f.get, g.get, h.get, i.get))
          else JsResults.collectFlatError(validations).get
        case _ => JsError("error.expected.jsarray.size.9")
      }
    }

  implicit def tuple9Writes[
  A: Writes, B: Writes, C: Writes, D: Writes, E: Writes,
  F: Writes, G: Writes, H: Writes, I: Writes]: Writes[(A, B, C, D, E, F, G, H, I)] =
    new Writes[(A, B, C, D, E, F, G, H, I)] {
      override def writes(o: (A, B, C, D, E, F, G, H, I)): JsValue = JsArray(Seq(
        Json.toJson(o._1),
        Json.toJson(o._2),
        Json.toJson(o._3),
        Json.toJson(o._4),
        Json.toJson(o._5),
        Json.toJson(o._6),
        Json.toJson(o._7),
        Json.toJson(o._8),
        Json.toJson(o._9)
      ))
    }

  implicit def tuple10Reads[
  A: Reads, B: Reads, C: Reads, D: Reads, E: Reads,
  F: Reads, G: Reads, H: Reads, I: Reads, J: Reads]: Reads[(A, B, C, D, E, F, G, H, I, J)] =
    new Reads[(A, B, C, D, E, F, G, H, I, J)] {
      override def reads(json: JsValue): JsResult[(A, B, C, D, E, F, G, H, I, J)] = json.validate[Seq[JsValue]] flatMap {
        case Seq(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10) =>
          val a = _1.validate[A]
          val b = _2.validate[B]
          val c = _3.validate[C]
          val d = _4.validate[D]
          val e = _5.validate[E]
          val f = _6.validate[F]
          val g = _7.validate[G]
          val h = _8.validate[H]
          val i = _9.validate[I]
          val j = _10.validate[J]
          val validations = Seq(a, b, c, d, e, f, g, h, i, j)
          if (validations.forall(_.isSuccess))
            JsSuccess((a.get, b.get, c.get, d.get, e.get, f.get, g.get, h.get, i.get, j.get))
          else JsResults.collectFlatError(validations).get
        case _ => JsError("error.expected.jsarray.size.10")
      }
    }

  implicit def tuple10Writes[
  A: Writes, B: Writes, C: Writes, D: Writes, E: Writes,
  F: Writes, G: Writes, H: Writes, I: Writes, J: Writes]: Writes[(A, B, C, D, E, F, G, H, I, J)] =
    new Writes[(A, B, C, D, E, F, G, H, I, J)] {
      override def writes(o: (A, B, C, D, E, F, G, H, I, J)): JsValue = JsArray(Seq(
        Json.toJson(o._1),
        Json.toJson(o._2),
        Json.toJson(o._3),
        Json.toJson(o._4),
        Json.toJson(o._5),
        Json.toJson(o._6),
        Json.toJson(o._7),
        Json.toJson(o._8),
        Json.toJson(o._9),
        Json.toJson(o._10)
      ))
    }
}

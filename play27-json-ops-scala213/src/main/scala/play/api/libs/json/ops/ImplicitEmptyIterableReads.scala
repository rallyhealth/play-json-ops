package play.api.libs.json.ops

import play.api.libs.json._

/**
 * Creates implicits for reading empty collection types, such as List[Nothing].
 *
 * Useful for avoiding compile-time errors when decoding as an empty collection,
 * where the compiler cannot or does not need to infer the item type of the collection.
 */
private[ops] trait ImplicitEmptyIterableReads {

  implicit val readsEmptyIterable: Reads[Iterable[Nothing]] = Reads {
    case JsArray(arr) if arr.isEmpty =>
      JsSuccess(Iterable.empty)
    case unexpected =>
      JsError(s"Unexpected value for empty iterable reader: $unexpected")
  }

  implicit val readsEmptySeq: Reads[Seq[Nothing]] = readsEmptyIterable.map(_.toSeq)

  implicit val readsEmptyList: Reads[List[Nothing]] = readsEmptyIterable.map(_.toList)

  implicit val writesEmptyIterable: Writes[Iterable[Nothing]] = Writes(_ => Json.arr())
}

package play.api.libs.json.ops

import play.api.libs.json._

import scala.language.implicitConversions
import scala.reflect._

case class TypeKeyExtractor[-T](getType: T => String, typeFieldName: String)

object FormatAbstractOps {

  def extractTypeKey[T](getType: T => String, jsonFieldName: String): TypeKeyExtractor[T] =
    TypeKeyExtractor[T](getType, jsonFieldName)

  def typed[Concrete, Abstract >: Concrete : TypeKeyExtractor](objFormat: OFormat[Concrete]): OFormat[Concrete] = {
    new OFormat[Concrete] {
      private[this] val extractor: TypeKeyExtractor[Concrete] = implicitly

      override def reads(json: JsValue): JsResult[Concrete] = objFormat.reads(json)

      override def writes(o: Concrete): JsObject = {
        val obj = objFormat.writes(o)
        obj ++ Json.obj(
          extractor.typeFieldName -> extractor.getType(o)
        )
      }
    }
  }

  def choose[T: ClassTag : TypeKeyExtractor](choose: PartialFunction[String, OFormat[_ <: T]]): Format[T] =
    new Format[T] {

      private[this] lazy val className = classTag[T].runtimeClass.getSimpleName

      private[this] val extractor: TypeKeyExtractor[T] = implicitly

      private[this] val chooseOrNone: String => Option[OFormat[_ <: T]] = choose.lift

      def findFormatOrThrow(typeKeyValue: String): OFormat[T] = {
        val format = chooseOrNone(typeKeyValue) getOrElse {
          throw new IllegalArgumentException(
            s"Cannot parse instance of $className. " +
              s"Unrecognized type key value '$typeKeyValue' for key field '${extractor.typeFieldName}'. " +
              s"Please handle this case by providing a Format for this type or define a way to parse this as an error."
          )
        }
        // safe to cast since all subclasses of T should be writable from the provided format
        format.asInstanceOf[OFormat[T]]
      }

      override def reads(json: JsValue): JsResult[T] = {
        (json \ extractor.typeFieldName).validate[String] flatMap {
          findFormatOrThrow(_).reads(json)
        }
      }

      override def writes(o: T): JsValue = {
        val typeKey = extractor.getType(o)
        val obj = findFormatOrThrow(typeKey).writes(o)
        obj ++ Json.obj(extractor.typeFieldName -> typeKey)
      }
    }
}

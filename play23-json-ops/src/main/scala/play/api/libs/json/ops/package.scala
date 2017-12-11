package play.api.libs.json

package object ops extends JsonImplicits {

  @deprecated("Use play.api.libs.json.ops.InvalidJsonException instead", "1.3.0")
  type InvalidJson = InvalidJsonException

  @deprecated("Use play.api.libs.json.ops.UnrecognizedTypeKeyException instead", "1.3.0")
  type UnrecognizedTypeKey = UnrecognizedTypeKeyException
}

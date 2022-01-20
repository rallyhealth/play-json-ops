[![Build Status](https://img.shields.io/travis/com/rallyhealth/play-json-ops)](https://app.travis-ci.com/github/rallyhealth/play-json-ops)
[![CodeCov](https://img.shields.io/codecov/c/github/rallyhealth/play-json-ops)](https://codecov.io/gh/rallyhealth/play-json-ops)

# Play Json Ops

Augments the [Play Json library](https://www.playframework.com/documentation/2.8.x/ScalaJson) with some helpful
implicits and tools for:

- Creating formats for traits and abstract classes
- Safely printing error messages with redacted sensitive data using implicit transformations
- Formats for tuples (up to arity-10) as JsArray
- Formats for scala.concurrent.Duration
- Safe formats for `Map` via `KeyReads` and `KeyWrites`  
- Format builder for empty collections
- UTCFormats for org.joda.time.DateTime
- ScalaCheck generators for JsValue, JsArray, and JsObject

# Versions

## 4.X Branch

| play version | scala versions | scalatest version | artifact name | artifact link |
| :---: | :---------------------: | :---: | :---------------------: | :-----------: |
| 2.8.x | 2.13.5, 2.12.6, 2.11.12 |       | play28-json-ops-v4      | ![Download](https://maven-badges.herokuapp.com/maven-central/com.rallyhealth/play28-json-ops-v4_2.13/badge.svg?style=flat) |
| 2.8.x | 2.13.5, 2.12.6, 2.11.12 | 3.2.x | play28-json-tests-sc14  | ![Download](https://maven-badges.herokuapp.com/maven-central/com.rallyhealth/play28-json-tests-sc14_2.13/badge.svg?style=flat) |
| 2.7.x | 2.13.5, 2.12.6, 2.11.12 |       | play27-json-ops-v4      | ![Download](https://maven-badges.herokuapp.com/maven-central/com.rallyhealth/play27-json-ops-v4_2.13/badge.svg?style=flat) |
| 2.7.x | 2.13.5, 2.12.6, 2.11.12 | 3.2.x | play27-json-tests-sc14  | ![Download](https://maven-badges.herokuapp.com/maven-central/com.rallyhealth/play27-json-tests-sc14_2.13/badge.svg?style=flat) |
| 2.6.x | 2.12.6, 2.11.12         |       | play26-json-ops-v4      | ![Download](https://maven-badges.herokuapp.com/maven-central/com.rallyhealth/play26-json-ops-v4_2.12/badge.svg?style=flat) |
| 2.6.x | 2.12.6, 2.11.12         | 3.0.x | play26-json-tests-sc13  | ![Download](https://maven-badges.herokuapp.com/maven-central/com.rallyhealth/play26-json-tests-sc13_2.12/badge.svg?style=flat) |
| 2.5.x | 2.11.12                 |       | play25-json-ops-v4      | ![Download](https://maven-badges.herokuapp.com/maven-central/com.rallyhealth/play25-json-ops-v4_2.11/badge.svg?style=flat) |
| 2.5.x | 2.11.12                 | 3.1.x | play25-json-tests-sc14  | ![Download](https://maven-badges.herokuapp.com/maven-central/com.rallyhealth/play25-json-tests-sc14_2.11/badge.svg?style=flat) |
| 2.5.x | 2.11.12                 | 3.0.x | play25-json-tests-sc13  | ![Download](https://maven-badges.herokuapp.com/maven-central/com.rallyhealth/play25-json-tests-sc13_2.11/badge.svg?style=flat) |
| 2.5.x | 2.11.12                 | 2.2.x | play25-json-tests-sc12  | ![Download](https://maven-badges.herokuapp.com/maven-central/com.rallyhealth/play25-json-tests-sc12_2.11/badge.svg?style=flat) |

## 3.X / 2.X Branches

These artifacts were published to Bintray, which was shutdown. These artifacts will NOT be ported to Maven Central.

# Getting Started

Pretty much all of these tools become available when you `import `[`play.api.libs.json.ops.v4._`](src/main/scala/play/api/libs/json/ops/package.scala)

## Dependencies

- [scalacheck-ops](https://github.com/rallyhealth/scalacheck-ops): for the ability to convert ScalaCheck `Gen` into an `Iterator`

# Features

## Implicits

By importing `play.api.libs.json.ops.v4._`, you get access to:

* `PlayJsonMacros.nullableReads` macro that will read `null` as `[]` for all container fields of a `case class`
* `Reads`, `Format`, and `OFormat` extension methods to recover from exceptions
* Many extension methods for the `play.api.libs.json.Json`
  - `Format.of[A]`, `OFormat.of[A]`, and `OWrites.of[A]` for summoning formats the same as `Reads.of[A]` and `Writes.of[A]`
  - `Format.asEither[A, B]` for reading and writing an either value based on some condition
  - `Format.asString[A]` for reading and writing a wrapper type as a string
  - `Format.pure` for reading and writing a constant value
  - `Format.empty` for reading or writing an empty collection
  - In Play 2.3, the `Json.format` and `Json.writes` macros would return `Format` and `Writes` instead of `OFormat` and
    `OWrites`, even though the macros would only produce these types. The play-json-ops for Play 2.3 provides a `Json.oformat`
    and `Json.owrites` which uses the underlying Play Json macros, but it casts the results.
* `Reads` and `Writes` implicits for tuple types (encoded as a `JsArray`)
* The `JsValue` extension method `.asOrThrow[A]` which throws a better exception that `.as[A]`
* And handy syntax for the features listed below

## Tolerant Container Reads Macro

Extending the `TolerantContainerFormats` trait or importing from its companion object will give you the ability to call
`.readNullableContainer` on a `Reads` instance. This will allow you to parse `null` fields as empty collections.

You can also use `PlayJsonMacros.nullableReads` to create a `Reads` for a `case class` that will accept either `null`
or missing field values for any container fields (`Seq`, `Set`, `Map`, etc) using the same method.

```scala
case class Example(values: Seq[Int])
object Example extends TolerantContainerFormats {

  val nonMacroExample: Reads[Seq[Int]] = (__ \ "values").readNullableContainer[Seq, Int]
  assert(Json.parse("null").as(nonMacroExample) == JsSuccess(Seq()))
  assert(Json.parse("[]").as[Example] == JsSuccess(Seq()))
  assert(Json.parse("[1]").as[Example] == JsSuccess(Seq(1)))

  val macroExample: Reads[Example] = PlayJsonMacros.nullableReads[Example]
  assert(Json.parse("{}").as(macroExample) == JsSuccess(Example(Seq())))
  assert(Json.parse("""{"values":null}""").as(macroExample) == JsSuccess(Example(Seq())))
  assert(Json.parse("""{"values":[]}""").as(macroExample) == JsSuccess(Example(Seq())))
  assert(Json.parse("""{"values":[1]}""").as(macroExample) == JsSuccess(Example(Seq(1))))
}
```

## Reads Recovery Methods

You can call `.recoverJsError`, `.recoverTotal`, or `.recoverWith` on a `Reads`, `Format`, or `OFormat` instance.
These methods allow you to recover from exceptions thrown during the reading process into an appropriate `JsResult`.

```scala
object ReadsRecoveryExamples {

  // converts all exceptions into a JsError with the exception captured as an argument in the JsonValidationError
  val readIntAsString = Reads.of[String].map(_.toInt).recoverJsError
  assert(readIntAsString.reads("not a number").isError) // no exception thrown

  // converts only the matched exceptions to JsResults, all others continue to throw
  val invertReader = Reads.of[String].map(1 / _.toDouble).recoverWith {
    case _: ArithmeticException => JsSuccess(Double.MaxValue) 
  }
  invertReader.reads("not a number") // throws NumberFormatException
  assert(invertReader.reads("0") == JsSuccess(Double.MaxValue)) // handles ArithmeticException

  // converts all exceptions into some value of the right type
  val readAbsValueOrSentinel = Reads.of[String].map(_.toInt.abs).recoverTotal(_ => -1)
  assert(readAbsValueOrSentinel.reads("not a number") == JsSuccess(-1))

  // these can be combined, of course
  val safeInvertReader = invertReader.recoverJsError
  assert(safeInvertReader.reads("not a number").isError) // no exception thrown
}
```

## Automatic Automated Tests

To get free test coverage, just extend `PlayJsonFormatSpec[T]` where `T` is a serializable type that you
would like to create a suite of tests for. All it requires is a ScalaCheck generator of the same type or
a sequence of examples.

This will use ScalaTest to create the test cases, however it will work just as well with Specs2

```scala

case class Example(value: String)

object Example {
  implicit val format = Json.format[Example]
}

object ExampleGenerators {
  implicit def arbExample(implicit arbString: Arbitrary[String]): Arbitrary[Example] =
    Arbitrary(arbString.map(Example(_)))
}

import ExampleGenerators._

// Free unit tests for serializing and deserializing Example values
// Also works with implicit Shrink[Example]
class ExampleFormatSpec extends PlayJsonFormatSpec[Example]

```

## Creating Formats for Traits and Abstract Classes

The following example shows how you can create a Format for the `Generic` trait using `Json.formatAbstract`.
This method requires an implicit `TypeKeyExtractor[Generic]`, which is used to pull a "key" value from some
field in the json / model. This key value is then matched on by a provided partial function from key to
format: `Any => OFormat[_ <: Generic]`.

The pattern works as follows:

1. Create the formats of each of the specific formats using `Json.formatWithType`
   and the `Json.format` macro.

   This will append the key field (even if it isn't in the case class constructor args) to the output json.

2. Create an implicit `TypeKeyExtractor` for the generic trait or abstract class on the companion object
   of that class.

   This is required for the `Json.formatWithType` to work properly and avoids repeating
   unnecessary boilerplate on each of the specific serializers to write out the key or the generic
   serializer to read the key.

3. Finally, define an implicit `Format` for your generic trait or abstract class using
   `Json.formatAbstract` by providing a partial function from the extracted key (from #2)
   to the specific serializer (from #1). Any unmatched keys will throw an exception.

```scala
import play.api.libs.json._
import play.api.libs.json.ops._

sealed trait Generic {
  def key: String
}

object Generic {

  implicit val extractor: TypeKeyExtractor[Generic] =
    Json.extractTypeKey[Generic].usingKeyField(_.key, __ \ "kind")

  implicit val format: OFormat[Generic] = Json.formatAbstract[Generic] {
    case SpecificA.key => OFormat.of[SpecificA]
    case SpecificB.key => OFormat.of[SpecificB]
  }
}

case class SpecificA(value: String) extends Generic {

  override def key: String = SpecificA.key
}

object SpecificA {
  final val key = "A"

  // NOTE: You will need to use Json.oformat for Play 2.3.x
  implicit val format: OFormat[SpecificA] = Json.formatWithTypeKeyOf[Generic].addedTo(Json.format[SpecificA])
}

case class SpecificB(value: String) extends Generic {

  override def key: String = SpecificB.key
}

object SpecificB {
  final val key = "B"

  implicit val format: OFormat[SpecificB] = Json.formatWithTypeKeyOf[Generic].addedTo(Json.format[SpecificB])
}

case object SpecificC extends Generic {
  final val key = "C"

  implicit val format: OFormat[this.type] = OFormat.pure(this, Generic.extractor.writeKeyToJson(this))
}
```

## Duration Formats

You can add implicit Json serializers by importing `DurationFormat.string` or `DurationFormat.array` depending
on the format you want.

You can also extend `ArrayDurationFormat` or `StringDurationFormat` for the same effect, but it requires that
you also extend an `ImplicitDurationReads`. A good default is to extend `ForgivingDurationReads` as this will
read either format.

Ok, now how the formats look in Json:

- `ArrayDurationFormat`

  ```json
  [1, "seconds"]
  ```

- `StringDurationFormat`

  ```json
  "1 second"
  ```

## ScalaCheck JsValue Generators

[ScalaCheck](http://scalacheck.org/) is a very simple and powerful library for property-based testing.

Fun fact: It is the only library dependency of the Scala compiler

Ok, so assuming you are already familiar with ScalaCheck now... Let's say you want to generate arbitrary
`JsValue`s or `JsObject`s. All you have to do is extend `JsValueGenerators` in your test class and voila!

By default the maximum depth of the `JsValue` trees is set to `JsValueGenerators.maxDepth` and the maximum
number of fields for `JsObject` and values for `JsArray` is set to `JsValueGenerators.maxWidth`. You can
override this in local scope by providing an implicit `Depth` or `Width` type value:

```scala
implicit val maxDepth: Depth = 4  

forAll() { (json: JsValue) =>
  // ...
}
```

or passing the values explicitly:

```scala

forAll(genJsValue(maxDepth = 4, maxWidth = 12)) { (json: JsValue) =>
  // ...
}
```

*Note:* I encountered a compiler bug when overriding implicits in a local scope where the compiler would
NOT throw the normal "ambiguous implicit values" exception and instead use the depth defined in the outer
scope. Just be sure not to define ambiguous implicit `Depth` and `Width` values and everything works great.

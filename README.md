[![Build Status](https://travis-ci.org/rallyhealth/play-json-ops.svg?branch=master)](https://travis-ci.org/rallyhealth/play-json-ops)
[![codecov](https://codecov.io/gh/rallyhealth/play-json-ops/branch/master/graph/badge.svg)](https://codecov.io/gh/rallyhealth/play-json-ops)

# Play Json Ops

Augments the [Play Json library](https://www.playframework.com/documentation/2.7.x/ScalaJson) with some helpful
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

## 3.X Branch

| play version | scala versions  | scalatest version | artifact name   | bintray |
| ------------ | --------------- | ----------------- | --------------- | ------- |
| 2.7.x        | 2.13.1          |                   | play27-json-ops | [ ![Download](https://api.bintray.com/packages/rallyhealth/maven/play27-json-ops/images/download.svg) ](https://bintray.com/rallyhealth/maven/play27-json-ops/_latestVersion) |
| 2.7.x        | 2.13.1          | 3.1.x             | play27-json-tests-sc14 | [ ![Download](https://api.bintray.com/packages/rallyhealth/maven/play27-json-tests-sc14/images/download.svg) ](https://bintray.com/rallyhealth/maven/play27-json-tests-sc14/_latestVersion) |
| 2.6.x        | 2.12.6, 2.11.12 |                   | play26-json-ops | [ ![Download](https://api.bintray.com/packages/rallyhealth/maven/play26-json-ops/images/download.svg) ](https://bintray.com/rallyhealth/maven/play26-json-ops/_latestVersion) |
| 2.6.x        | 2.12.6, 2.11.12 | 3.0.x             | play26-json-tests-sc13 | [ ![Download](https://api.bintray.com/packages/rallyhealth/maven/play26-json-tests-sc13/images/download.svg) ](https://bintray.com/rallyhealth/maven/play26-json-tests-sc13/_latestVersion) |
| 2.5.x        | 2.11.12         |                   | play25-json-ops | [ ![Download](https://api.bintray.com/packages/rallyhealth/maven/play25-json-ops/images/download.svg) ](https://bintray.com/rallyhealth/maven/play25-json-ops/_latestVersion) |
| 2.5.x        | 2.11.12         | 3.0.x             | play25-json-tests-sc13 | [ ![Download](https://api.bintray.com/packages/rallyhealth/maven/play25-json-tests-sc13/images/download.svg) ](https://bintray.com/rallyhealth/maven/play25-json-tests-sc13/_latestVersion) |
| 2.5.x        | 2.11.12         | 2.2.x             | play25-json-tests-sc12 | [ ![Download](https://api.bintray.com/packages/rallyhealth/maven/play25-json-tests-sc12/images/download.svg) ](https://bintray.com/rallyhealth/maven/play25-json-tests-sc12/_latestVersion) |

## 2.X Branch

| play version | scala versions  | scalatest version | artifact name          | artifact version |
| ------------ | --------------- | ----------------- | ---------------------- | ---------------- |
| 2.5.x        | 2.11.12         |                   | play25-json-ops        | 2.1.1            |
| 2.5.x        | 2.11.12         | 3.0.x             | play25-json-tests      | 2.1.1            |
| 2.5.x        | 2.11.12         | 2.2.x             | play25-json-tests-sc12 | 2.1.1            |
| 2.3.x        | 2.11.12         |                   | play23-json-ops        | 2.1.1            |
| 2.3.x        | 2.11.12         | 3.0.x             | play23-json-tests      | 2.1.1            |
| 2.3.x        | 2.11.12         | 2.2.x             | play23-json-tests-sc12 | 2.1.1            |

# Getting Started

Pretty much all of these tools become available when you `import `[`play.api.libs.json.ops._`](src/main/scala/play/api/libs/json/ops/package.scala)

## Dependencies

- [scalacheck-ops](https://github.com/rallyhealth/scalacheck-ops): for the ability to convert ScalaCheck `Gen` into an `Iterator`

# Features

## Implicits

By importing `play.api.libs.json.ops._`, you get access to implicits that provide:

* Many extension methods for the `play.api.libs.json.Json`
  - `Format.of[A]`, `OFormat.of[A]`, and `OWrites.of[A]` for summoning formats the same as `Reads.of[A]` and `Writes.of[A]`
  - `Format.asEither[A, B]` for reading and writing an either value based on some condition
  - `Format.asString[A]` for reading and writing a wrapper type as a string
  - `Format.pure` for reading and writing a constant value
  - `Format.empty` for reading or writing an empty collection
  - In Play 2.3, the `Json.format` and `Json.writes` macros would return `Format` and `Writes` instead of `OFormat` and
    `OWrites`, even though the macros would only produce these types. The play-json-ops for Play 2.3 provides a `Json.oformat`
    and `Json.owrites` which uses the underlying Play Json macros, but it casts the results.
* `Reads` and `Writes` for tuple types by writing the result as a `JsArray`
* The `JsValue` extension method `.asOrThrow[A]` which throws a better exception that `.as[A]`
* And handy syntax for the features listed below

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

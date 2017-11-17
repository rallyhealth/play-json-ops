<a href='https://travis-ci.org/jeffmay/play-json-ops'>
  <img src='https://travis-ci.org/jeffmay/play-json-ops.svg' alt='Build Status' />
</a>
<a href='https://coveralls.io/github/jeffmay/play-json-ops?branch=master'>
  <img src='https://coveralls.io/repos/jeffmay/play-json-ops/badge.svg?branch=master&service=github' alt='Coverage Status' />
</a>

# Play Json Ops

Augments the [Play Json library](https://www.playframework.com/documentation/2.5.x/ScalaJson) with some helpful
implicits and tools for:

- Creating formats for traits and abstract classes
- Safely printing error messages with redacted sensitive data using implicit transformations
- Formats for all tuples as JsArray
- ScalaCheck generators for JsValue, JsArray, and JsObject
- Formats for scala.concurrent.Duration
- UTCFormats for org.joda.time.DateTime
- Compile-time Json.oformat and Json.owrites macros (Play 2.3 only)

<h1>Versions</h1>

<h2>2.X Branch</h2>
<table>
  <tr>
    <th></th>
    <th></th>
    <th colspan=2>play-json-ops</th>
    <th colspan=2>play-json-tests</th>
  </tr>
  <tr>
    <th>play version</th>
    <th>scalatest version</th>
    <th>artifact name</th>
    <th>bintray</th>
    <th>artifact name</th>
    <th>bintray</th>
  </tr>
  <tr>
    <th>2.5.x</th>
    <th>3.0.x</th>
    <td>play25-json-ops</td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play25-json-ops/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play25-json-ops/images/download.svg'>
      </a>
    </td>
    <td>play25-json-tests</td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play25-json-tests/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play25-json-tests/images/download.svg'>
      </a>
    </td>
  </tr>
  <tr>
    <th>2.5.x</th>
    <th>2.2.x</th>
    <td>play25-json-ops</td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play25-json-ops/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play25-json-ops/images/download.svg'>
      </a>
    </td>
    <td>play25-json-tests-sc12</td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play25-json-tests-sc12/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play25-json-tests-sc12/images/download.svg'>
      </a>
    </td>
  </tr>
  <tr>
    <th>2.3.x</th>
    <th>3.0.x</th>
    <td>play23-json-ops</td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play23-json-ops/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play23-json-ops/images/download.svg'>
      </a>
    </td>
    <td>play23-json-tests</td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play23-json-tests/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play23-json-tests/images/download.svg'>
      </a>
    </td>
  </tr>
  <tr>
    <th>2.3.x</th>
    <th>2.2.x</th>
    <td>play23-json-ops</td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play23-json-ops/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play23-json-ops/images/download.svg'>
      </a>
    </td>
    <td>play23-json-tests-sc12</td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play23-json-tests-sc12/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play23-json-tests-sc12/images/download.svg'>
      </a>
    </td>
  </tr>
</table>

<h2>1.X Branch</h2>
<table>
  <tr>
    <th></th>
    <th></th>
    <th colspan=2>play-json-ops</th>
    <th colspan=2>play-json-tests</th>
  </tr>
  <tr>
    <th>play version</th>
    <th>scalatest version</th>
    <th>artifact name</th>
    <th>bintray</th>
    <th>artifact name</th>
    <th>bintray</th>
  </tr>
  <tr>
    <th>2.5.x</th>
    <th>3.0.x</th>
    <td>play-json-ops-25</td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play-json-ops-25/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play-json-ops-25/images/download.svg'>
      </a>
    </td>
    <td>play25-json-tests</td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play25-json-tests/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play25-json-tests/images/download.svg'>
      </a>
    </td>
  </tr>
  <tr>
    <th>2.5.x</th>
    <th>2.2.x</th>
    <td>play-json-ops-25</td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play-json-ops-25/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play-json-ops-25/images/download.svg'>
      </a>
    </td>
    <td>play-json-tests-25</td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play-json-tests-25/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play-json-tests-25/images/download.svg'>
      </a>
    </td>
  </tr>
  <tr>
    <th>2.3.x</th>
    <th>3.0.x</th>
    <td>play-json-ops</td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play-json-ops/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play-json-ops/images/download.svg'>
      </a>
    </td>
    <td>play23-json-tests</td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play23-json-tests/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play23-json-tests/images/download.svg'>
      </a>
    </td>
  </tr>
  <tr>
    <th>2.3.x</th>
    <th>2.2.x</th>
    <td>play-json-ops</td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play-json-ops/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play-json-ops/images/download.svg'>
      </a>
    </td>
    <td>play-json-tests</td>
    <td>
      <a href='https://bintray.com/jeffmay/maven/play-json-tests/_latestVersion'>
        <img src='https://api.bintray.com/packages/jeffmay/maven/play-json-tests/images/download.svg'>
      </a>
    </td>
  </tr>
</table>

# Getting Started

Pretty much all of these tools become available when you `import `[`play.api.libs.json.ops._`](src/main/scala/play/api/libs/json/ops/package.scala)

## Dependencies

- [scalacheck-ops](https://github.com/jeffmay/scalacheck-ops): for the ability to convert ScalaCheck `Gen` into an `Iterator`

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

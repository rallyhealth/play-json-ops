package play.api.libs.json.ops

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import play.api.libs.json.{Json, Reads}

import scala.collection.mutable

class PlayJsonMacrosSpec extends AnyFunSpec with Matchers {

  case class TestA(strings: List[String])
  object TestA extends TolerantContainerFormats {
    implicit val TestAReads: Reads[TestA] = PlayJsonMacros.nullableReads[TestA]
  }

  case class TestB(strings: Seq[String])
  object TestB extends TolerantContainerFormats {
    implicit val TestBReads: Reads[TestB] = PlayJsonMacros.nullableReads[TestB]
  }

  case class TestC(testA: Set[TestA], testB: mutable.ArraySeq[TestB])
  object TestC extends TolerantContainerFormats {
    implicit val TestCReads: Reads[TestC] = PlayJsonMacros.nullableReads[TestC]
  }

  describe("PlayJsonMacrosSpec") {
    it("should deserialize a TestA when TestA is populated") {
      val jsonStr =
        """
          |{"strings":["string1", "string2"]}
        """.stripMargin
      val json = Json.parse(jsonStr)
      val testA = json.as[TestA]

      assert(testA == TestA(List("string1", "string2")))
    }

    it("should deserialize a TestA when TestA is empty") {
      val jsonStr =
        """
          |{"strings":[]}
        """.stripMargin
      val json = Json.parse(jsonStr)
      val testA = json.as[TestA]

      assert(testA == TestA(List.empty[String]))
    }

    it("should deserialize a TestA when TestA has field missing") {
      val jsonStr =
        """
          |{}
        """.stripMargin
      val json = Json.parse(jsonStr)
      val testA = json.as[TestA]

      assert(testA == TestA(List.empty[String]))
    }

    it("should deserialize a TestB when TestB is populated") {
      val jsonStr =
        """
          |{"strings":["string1", "string2"]}
        """.stripMargin
      val json = Json.parse(jsonStr)
      val testB = json.as[TestB]

      assert(testB == TestB(List("string1", "string2")))
    }

    it("should deserialize a TestB when TestB is empty") {
      val jsonStr =
        """
          |{"strings":[]}
        """.stripMargin
      val json = Json.parse(jsonStr)
      val testB = json.as[TestB]

      assert(testB == TestB(List.empty[String]))
    }

    it("should deserialize a TestB when TestB has field missing") {
      val jsonStr =
        """
          |{}
        """.stripMargin
      val json = Json.parse(jsonStr)
      val testB = json.as[TestB]

      assert(testB == TestB(List.empty[String]))
    }

    it("should deserialize a TestC when TestC is populated") {
      val jsonStr =
        """
          |{
          |"testA":[{"strings":["stringZ", "stringX"]}],
          |"testB":[{"strings":["stringA", "stringB"]}]
          |}
        """.stripMargin
      val json = Json.parse(jsonStr)
      val testC = json.as[TestC]

      assert(testC == TestC(Set(TestA(List("stringZ", "stringX"))), mutable.ArraySeq(TestB(List("stringA", "stringB")))))
    }

    it("should deserialize a TestC when parts of testC are empty") {
      val jsonStr =
        """
          |{
          |"testA":[{}],
          |"testB":[]
          |}
        """.stripMargin
      val json = Json.parse(jsonStr)
      val testC = json.as[TestC]

      assert(testC == TestC(Set(TestA(List.empty[String])), mutable.ArraySeq.empty[TestB]))
    }

    it("should deserialize a TestC when parts of testC are missing") {
      val jsonStr =
        """
          |{
          |"testB":[]
          |}
        """.stripMargin
      val json = Json.parse(jsonStr)
      val testC = json.as[TestC]

      assert(testC == TestC(Set.empty[TestA], mutable.ArraySeq.empty[TestB]))
    }

    it("should deserialize a TestC when all of testC is missing") {
      val jsonStr =
        """
          |{}
        """.stripMargin
      val json = Json.parse(jsonStr)
      val testC = json.as[TestC]

      assert(testC == TestC(Set.empty[TestA], mutable.ArraySeq.empty[TestB]))
    }
  }
}

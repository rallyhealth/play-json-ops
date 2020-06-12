package play.api.libs.json.ops.v4

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import play.api.libs.json._

class ReadsRecoverOpsSpec extends AnyFreeSpec {

  private val it = classOf[ReadsRecoverOps[_]].getSimpleName

  s"$it.recoverJsError should recover from exceptions with a JsError" in {
    val readStringAsInt = Reads.of[String].map(_.toInt).recoverJsError
    readStringAsInt.reads(JsString("not a number")) match {
      case JsError(Seq((JsPath, Seq(JsonValidationError(Seq(message), ex))))) =>
        assertResult("error.expected.int")(message)
        ex shouldBe a[NumberFormatException]
      case o => fail(s"Expected a single error message with a single exception, not $o")
    }
  }

  s"$it.recoverWith should throw any exception not caught by the partial function" in {
    val readStringAsIntInverted = Reads.of[String].map(1 / _.toInt).recoverWith {
      case ex: NumberFormatException => RecoverOps.expectedTypeError(classOf[Int], ex)
    }
    // it should catch format exceptions
    assert(readStringAsIntInverted.reads(JsString("not a number")).isError)
    // but math exceptions are uncaught
    an[ArithmeticException] shouldBe thrownBy {
      readStringAsIntInverted.reads(JsString("0"))
    }
  }

  s"$it.recoverTotal should call the recover function" in {
    val readStringAsIntInverted = Reads.of[String].map(_.toInt.abs).recoverTotal(_ => -1)
    assertResult(JsSuccess(-1))(readStringAsIntInverted.reads(JsString("not a number")))
  }
}

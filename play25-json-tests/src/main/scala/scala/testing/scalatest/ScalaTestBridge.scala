package scala.testing.scalatest

import org.scalatest.{Suite, TestRegistration}

import scala.testing.TestSuiteBridge

/**
 * Provides the operations required for [[TestSuiteBridge]], using the methods from ScalaTest's [[Suite]].
 *
 * Extend this class along with your preferred flavor of [[Suite]] to fulfill the required operations
 * for [[TestSuiteBridge]].
 */
trait ScalaTestBridge extends TestSuiteBridge with Suite with TestRegistration {

  override protected def assertEqual[T](left: T, right: T): Unit = assert(left == right)

  override def fail(): Nothing                                  = super[TestSuiteBridge].fail()
  override def fail(message: String): Nothing                   = super[TestSuiteBridge].fail(message)
  override def fail(message: String, cause: Throwable): Nothing = super[TestSuiteBridge].fail(message, cause)
  override def fail(cause: Throwable): Nothing                  = super[TestSuiteBridge].fail(cause)

  override protected def doFail(optReason: Option[String], optCause: Option[Throwable]): Nothing = {
    (optReason, optCause) match {
      case (Some(reason), Some(cause)) => super[Suite].fail(reason, cause)
      case (Some(reason), None)        => super[Suite].fail(reason)
      case (None, Some(cause))         => super[Suite].fail(cause)
      case (None, None)                => super[Suite].fail()
    }
  }

  override protected[testing] def registerTests(tests: Map[String, () => Unit]): Unit = {
    for ((name, test) <- tests) {
      registerTest(name)(test())
    }
  }

  // register the tests after the Suite has been initialized
  registerTests()
}
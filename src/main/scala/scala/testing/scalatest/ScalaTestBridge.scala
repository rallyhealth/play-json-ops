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

  override def fail(reason: String, cause: Option[Throwable]): Nothing = cause match {
    case Some(ex) => super[Suite].fail(reason, ex)
    case None     => super[Suite].fail(reason)
  }

  override protected[testing] def registerTests(tests: Map[String, () => Unit]): Unit = {
    for ((name, test) <- tests) {
      registerTest(name)(test())
    }
  }

  // register the tests after the Suite has been initialized
  registerTests()
}
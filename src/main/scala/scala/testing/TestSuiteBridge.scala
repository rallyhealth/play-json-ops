package scala.testing

/**
 * Defines a generic set of operations required to setup tests to run in the desired test framework.
 *
 * This allows libraries to provide generic test suites that can be extended for free automated tests.
 * Yes, FREE! This allows you to inherit automated tests, and get them... automatically.
 *
 * There is currently only one subclass for this [[scala.testing.scalatest.ScalaTestBridge]]
 */
trait TestSuiteBridge extends GenericTestSuite {

  protected def assertEqual[T](left: T, right: T): Unit

  protected def fail(reason: String, cause: Option[Throwable] = None): Nothing

  protected[testing] def registerTests(tests: Map[String, () => Unit]): Unit

  /**
   * Called after the Suite has extended the required implementation for registering tests.
   */
  protected[testing] def registerTests(): Unit = registerTests(tests)
}

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

  /**
   * Use the underlying test framework's assertion method to compare the left element to the right.
   */
  protected def assertEqual[T](left: T, right: T): Unit

  /**
   * A convenient test failure method that throws an exception.
   *
   * @note This should always call the [[doFail]]
   */
  protected def fail(): Nothing = doFail(None, None)

  protected def fail(reason: String): Nothing = doFail(Some(reason), None)

  protected def fail(cause: Throwable): Nothing = doFail(None, Some(cause))

  protected def fail(reason: String, cause: Throwable): Nothing = doFail(Some(reason), Some(cause))

  /**
   * All convenience fail methods should come through this fail method.
   *
   * @note The above fail methods are not marked final because they may collide with the methods
   *       provided by the test library, however, they should only be overriden to call this method.
   *
   * @param optReason the reason for the test failure
   * @param optCause the exception that caused the test failure, if available
   * @return
   */
  protected def doFail(optReason: Option[String], optCause: Option[Throwable]): Nothing

  protected[testing] def registerTests(tests: Map[String, () => Unit]): Unit

  /**
   * Called after the Suite has extended the required implementation for registering tests.
   */
  protected[testing] def registerTests(): Unit = registerTests(tests)
}

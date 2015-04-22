package scala.testing

/**
 * A trait that provides early initialization of the tests in a way that is agnostic to any test runner.
 *
 * Any generic tests should extend from this trait to ensure that any registered tests are delayed until
 * the appropriate [[TestSuiteBridge]] (and associated Suite class) has been constructed.
 *
 * The self-type [[TestSuiteBridge]] should be carried along as a self-type to provide a better error
 * message for the implementing test suite.
 *
 * It will construct a holder for the tests before executing the body of the test suite runs to avoid
 * any NullPointerExceptions when attempting to register a test.
 */
trait GenericTestSuite {
  self: TestSuiteBridge =>

  protected[testing] var tests: Map[String, () => Unit] = Map.empty

  protected def addTest(name: String)(f: => Unit): Unit = {
    tests += (name, () => f)
  }
}

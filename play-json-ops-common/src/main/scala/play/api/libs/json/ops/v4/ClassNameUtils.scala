package play.api.libs.json.ops.v4

private[v4] object ClassNameUtils {

  /**
   * Similar to the Class.getSimpleName method, except it does not throw any exceptions and
   * handles Scala inner classes better.
   */
  def safeSimpleClassName(cls: Class[_]): String = {
    // This logic is designed to be robust without much noise
    // 1. use getName to avoid runtime exceptions from getSimpleName
    // 2. filter out '$' anonymous class / method separators
    // 3. start the full class name from the first upper-cased outer class name
    //    (to avoid picking up unnecessary package names)
    cls.getName
      .split('.')
      .last // safe because Class names will never be empty in any realistic scenario
      .split('$')
      .mkString(".")
  }
}

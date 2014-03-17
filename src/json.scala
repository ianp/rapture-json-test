package rapture.json.test

import rapture.core._
import rapture.json._
import rapture.test._

import strategy.throwExceptions
import jsonParsers.jackson._

class Test extends TestSuite {

  val source1 = json"""{
    "string": "Hello",
    "int": 42,
    "double": 3.14159,
    "boolean": true,
    "list": [1, 2, 3],
    "foo": { "alpha": "test", "beta": 1 },
    "bar": { "foo": { "alpha": "test2", "beta": 2 }, "gamma": 2.7 }
  }"""

  case class Foo(alpha: String, beta: Int)
  case class Bar(foo: Foo, gamma: Double)

  val `Extract Int` = test {
    source1.int.get[Int]
  } yields 42
  
  val `Extract String` = test {
    source1.string.get[String]
  } yields "Hello"
  
  val `Extract Double` = test {
    source1.double.get[Double]
  } yields 3.14159
  
  val `Extract Boolean` = test {
    source1.boolean.get[Boolean]
  } yields true
  
  val `Extract List[Int]` = test {
    source1.list.get[List[Int]]
  } requires `Extract Int` yields List(1, 2, 3)
  
  val `Extract case class` = test {
    source1.foo.get[Foo]
  } requires (
    `Extract String`,
    `Extract Int`
  ) yields Foo("test", 1)
  
  val `Extract nested case class` = test {
    source1.bar.get[Bar]
  } requires (
    `Extract case class`
  ) yields Bar(Foo("test2", 2), 2.7)
  
  val `Extract List element` = test {
    source1.list(1).get[Int]
  } requires (
    `Extract Int`
  ) yields 2
  
  val `Extract object element` = test {
    source1.bar.foo.alpha.get[String]
  } requires (
    `Extract String`
  ) yields "test2"


  val `Check type failure` = test {
    source1.string.get[Int]
  } throws TypeMismatchException(JsonTypes.String, JsonTypes.Number, Vector(Right("string")))

}

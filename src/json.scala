package rapture.json.test

import rapture.core._
import rapture.json._
import rapture.test._

import strategy.throwExceptions

import scala.reflect._

class JsonTests()(implicit val parser: JsonParser[String]) extends TestSuite {

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

  val `Check missing value failure` = test {
    source1.nothing.get[Int]
  } throws MissingValueException(Vector(Right("nothing")))

  val `Match string` = test {
    source1 match {
      case json""" { "string": $h } """ => h.get[String]
    }
  } yields "Hello"

  val `Match inner JSON` = test {
    source1 match {
      case json""" { "foo": $foo } """ => foo
    }
  } yields json"""{ "alpha": "test", "beta": 1 }"""
  
  val `Match inner string` = test {
    source1 match {
      case json""" { "foo": { "alpha": $t } } """ => t.get[String]
    }
  } yields "test"
  
  val `Filtered match` = test {
    source1 match {
      case json""" { "int": 42, "foo": { "alpha": $t } } """ => t.get[String]
    }
  } yields "test"
  
  val `Inner filtered match` = test {
    source1 match {
      case json""" { "foo": { "alpha": "test" }, "bar": { "gamma": $g } } """ => g.get[Double]
    }
  } yields 2.7
  
  val `Filtered failed match` = test {
    source1 match {
      case json""" { "int": 0, "foo": { "alpha": $t } } """ => t.get[String]
    }
  } throws classTag[MatchError]
}

class MutableJsonTests()(implicit val parser: JsonBufferParser[String]) extends TestSuite {
  
  case class Foo(alpha: String, beta: Int)
  case class Bar(foo: Foo, gamma: Double)
  
  val source2 = JsonBuffer.parse("""{
    "string": "Hello",
    "int": 42
  }""")

  val `Mutable get String` = test {
    source2.string.get[String]
  } yields "Hello"

  val `Mutable get Int` = test {
    source2.int.get[Int]
  } yields 42

  val `Mutable change String` = test {
    source2.string = "World"
    source2.string.get[String]
  } yields "World"

  val `Mutable add String` = test {
    source2.inner.newString = "Hello"
    source2.inner.newString.get[String]
  } yields "Hello"
  
  val `Mutable add case class` = test {
    source2.foo = Foo("string", -1)
    println(source2)
    source2.foo.get[Foo]
  } yields Foo("string", -1)
}

class JawnTest extends JsonTests()(jsonParsers.jawn.jawnStringParser)

class JacksonTest extends JsonTests()(jsonParsers.jackson.jacksonStringParser)

class ScalaJsonTest extends JsonTests()(jsonParsers.scalaJson.scalaJsonParser)

class ScalaJsonMutableTests extends MutableJsonTests()(jsonParsers.scalaJson.scalaJsonParser)

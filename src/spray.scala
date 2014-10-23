package rapture.json.test.spray

import rapture.core._
import rapture.json._
import rapture.data._
import rapture.test._

case class Foo(alpha: String, beta: Int)
case class Bar(foo: Foo, gamma: Double)

case class A(a: B)
case class B(b: C)
case class C(c: D)
case class D(d: E)
case class E(e: F)
case class F(f: Int)

class JsonTests() extends TestSuite {

  import jsonBackends.spray._

  val source1 = json"""{
    "string": "Hello",
    "int": 42,
    "double": 3.14159,
    "boolean": true,
    "list": [1, 2, 3],
    "foo": { "alpha": "test", "beta": 1 },
    "bar": { "foo": { "alpha": "test2", "beta": 2 }, "gamma": 2.7 }
  }"""

  val `Extract Int` = test {
    source1.int.as[Int]
  } yields 42
  
  val `Extract String` = test {
    source1.string.as[String]
  } yields "Hello"
  
  val `Extract Double` = test {
    source1.double.as[Double]
  } yields 3.14159
  
  val `Extract Boolean` = test {
    source1.boolean.as[Boolean]
  } yields true
  
  val `Extract List[Int]` = test {
    source1.list.as[List[Int]]
  } requires `Extract Int` yields List(1, 2, 3)
  
  val `Extract Vector[Int]` = test {
    source1.list.as[Vector[Int]]
  } requires `Extract Int` yields Vector(1, 2, 3)
  
  val `Extract case class` = test {
    source1.foo.as[Foo]
  } requires (
    `Extract String`,
    `Extract Int`
  ) yields Foo("test", 1)
  
  val `Extract nested case class` = test {
    source1.bar.as[Bar]
  } requires (
    `Extract case class`
  ) yields Bar(Foo("test2", 2), 2.7)
 
  val `Extract deeply-nested case class` = test {
    json"""{ "a": { "b": { "c": { "d": { "e": { "f": 1 } } } } } }""".as[A]
  } yields A(B(C(D(E(F(1))))))

  val `Extract List element` = test {
    source1.list(1).as[Int]
  } requires (
    `Extract Int`
  ) yields 2
  
  val `Extract object element` = test {
    source1.bar.foo.alpha.as[String]
  } requires (
    `Extract String`
  ) yields "test2"

  val `Check type failure` = test {
    source1.string.as[Int]
  } throws TypeMismatchException(DataTypes.String, DataTypes.Number, Vector(Right("string")))

  val `Check missing value failure` = test {
    source1.nothing.as[Int]
  } throws MissingValueException(Vector(Right("nothing")))

  val `Match string` = test {
    source1 match {
      case json""" { "string": $h } """ => h.as[String]
    }
  } yields "Hello"

  val `Match inner JSON` = test {
    source1 match {
      case json""" { "foo": $foo } """ => foo
    }
  } yields json"""{ "alpha": "test", "beta": 1 }"""
  
  val `Match inner string` = test {
    source1 match {
      case json""" { "foo": { "alpha": $t } } """ => t.as[String]
    }
  } yields "test"
  
  val `Filtered match` = test {
    source1 match {
      case json""" { "int": 42, "foo": { "alpha": $t } } """ => t.as[String]
    }
  } yields "test"
  
  val `Inner filtered match` = test {
    source1 match {
      case json""" { "foo": { "alpha": "test" }, "bar": { "gamma": $g } } """ => g.as[Double]
    }
  } yields 2.7
  
  val `Filtered failed match` = test {
    source1 match {
      case json""" { "int": 0, "foo": { "alpha": $t } } """ => t.as[String]
    }
  } throws classOf[MatchError]

  val `Multiple pattern match` = test {
    json"""{ "foo": "bar" }""" match {
      case json"""{ "bar": "foo" }""" => 0
      case json"""{ "foo": "baz" }""" => 1
      case json"""{ "foo": "bar" }""" => 2
    }
  } yields 2

  val `Empty object doesn't match` = test {
    json"""{ "foo": "bar" }""" match {
      case json"""{ "foo": {} }""" => 0
    }
  } throws classOf[MatchError]

  val `Serialize string` = test {
    Json("Hello World!").toString
  } yields """"Hello World!""""

  val `Serialize int` = test {
    Json(1648).toString
  } yields "1648"

  val `Serialize array` = test {
    Json(List(1, 2, 3)).toString
  } yields """[
             | 1,
             | 2,
             | 3
             |]""".stripMargin

  val `Serialize object` = test {
    import formatters.humanReadable
    Json.format(json"""{"foo":"bar","baz":"quux"}""")
  } yields """{
             | "foo": "bar",
             | "baz": "quux"
             |}""".stripMargin
  
  val `Empty object serialization` = test {
    import formatters.humanReadable
    Json.format(json"{}")
  } yields "{}"
  
  val `Empty array serialization` = test {
    import formatters.humanReadable
    Json.format(json"[]")
  } yields "[]"
}

class MutableJsonTests() extends TestSuite {
 
  import jsonBackends.spray._

  case class Foo(alpha: String, beta: Int)
  case class Bar(foo: Foo, gamma: Double)
  
  val source2 = JsonBuffer.parse("""{
    "string": "Hello",
    "int": 42
  }""")

  val `Mutable get String` = test {
    source2.string.as[String]
  } yields "Hello"

  val `Mutable get Int` = test {
    source2.int.as[Int]
  } yields 42

  val `Mutable change String` = test {
    source2.string = "World"
    source2.string.as[String]
  } yields "World"

  val `Mutable add String` = test {
    source2.inner.newString = "Hello"
    source2.inner.newString.as[String]
  } yields "Hello"
 
  val `Mutable add Json` = test {
    val jb = JsonBuffer.empty
    jb.foo = json"""{ "foo": "bar" }"""
  } yields jsonBuffer"""{ "foo": { "foo": "bar" } }"""

  val `Mutable add case class` = test {
    source2.foo = Foo("string", -1)
    source2.foo.as[Foo]
  } yields Foo("string", -1)
 
  val `Deep insertion of integer` = test {
    source2.alpha.beta.gamma.delta = 1
    source2.alpha.beta.gamma.delta.as[Int]
  } yields 1

  val `Array autopadding` = test {
    source2.autopad(4) = 1
    source2.autopad(4).as[Int]
  } yields 1

  val `Deep array insertion of integer` = test {
    source2.array(1)(2)(3)(4) = 1
    source2.array(1)(2)(3)(4).as[Int]
  } requires `Array autopadding` yields 1

  val `Deep mixed insertion of string` = test {
    source2.mixed(4).foo.bar(2).baz = "Mixed"
    source2.mixed(4).foo.bar(2).baz.as[String]
  } requires `Array autopadding` yields "Mixed"

  val `Mutable add array String` = test {
    source2.inner.newArray += "Hello"
    source2.inner.newArray(0).as[String]
  } yields "Hello"
  
}

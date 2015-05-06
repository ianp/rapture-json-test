package rapture.json.test

import rapture.core._
import rapture.json._
import rapture.data._
import rapture.test._

import scala.util

case class Foo(alpha: String, beta: Int)
case class Bar(foo: Foo, gamma: Double)

case class Baz(alpha: String, beta: Option[Int])
case class Baz2(alpha: String, beta: util.Try[Int])

case class A(a: B)
case class B(b: C)
case class C(c: D)
case class D(d: E)
case class E(e: F)
case class F(f: Int)

import jsonBackends._
class ScalaJsonTests() extends JsonTests(scalaJson.implicitJsonAst, scalaJson.implicitJsonStringParser)
class PlayTests() extends JsonTests(play.implicitJsonAst, play.implicitJsonStringParser)
class JawnTests() extends JsonTests(jawn.implicitJsonAst, jawn.implicitJsonStringParser(jawn.jawnFacade))
class Json4sTests() extends JsonTests(json4s.implicitJsonAst, json4s.implicitJsonStringParser)
class SprayTests() extends JsonTests(spray.implicitJsonAst, spray.implicitJsonStringParser)
class JacksonTests() extends JsonTests(jackson.implicitJsonAst, jackson.implicitJsonStringParser)
class ArgonautTests() extends JsonTests(argonaut.implicitJsonAst, argonaut.implicitJsonStringParser)
class LiftTests() extends JsonTests(lift.implicitJsonAst, lift.implicitJsonStringParser)

class MutableScalasonTests() extends MutableJsonTests(scalaJson.implicitJsonAst, scalaJson.implicitJsonStringParser)
class MutablePlayTests() extends MutableJsonTests(play.implicitJsonAst, play.implicitJsonStringParser)
class MutableJawnTests() extends MutableJsonTests(jawn.implicitJsonAst, jawn.implicitJsonStringParser(jawn.jawnFacade))
class MutableJson4sTests() extends MutableJsonTests(json4s.implicitJsonAst, json4s.implicitJsonStringParser)
class MutableSprayTests() extends MutableJsonTests(spray.implicitJsonAst, spray.implicitJsonStringParser)
class MutableArgonautTests() extends MutableJsonTests(argonaut.implicitJsonAst, argonaut.implicitJsonStringParser)
class MutableLiftTests() extends MutableJsonTests(lift.implicitJsonAst, lift.implicitJsonStringParser)

abstract class JsonTests(ast: JsonAst, parser: Parser[String, JsonAst]) extends TestSuite {

  implicit def implicitAst: JsonAst = ast
  implicit def implicitParser: Parser[String, JsonAst] = parser

  import formatters.humanReadable._

  val source1 = json"""{
    "string": "Hello",
    "int": 42,
    "double": 3.14159,
    "boolean": true,
    "list": [1, 2, 3],
    "foo": { "alpha": "test", "beta": 1 },
    "bar": { "foo": { "alpha": "test2", "beta": 2 }, "gamma": 2.7 },
    "baz": { "alpha": "test" },
    "baz2": { "alpha": "test", "beta": 7 },
    "self": 0
  }"""

  val `Extract Int` = test {
    source1.int.as[Int]
  } returns 42
  val `Extract value called "self"` = test {
    source1.self.as[Int]
  } returns 0
  
  val `Extract Option[Int]` = test {
    source1.int.as[Option[Int]]
  } returns Some(42)
  
  val `Extract Option[Int], wrong type` = test {
    source1.string.as[Option[Int]]
  } returns None
 
  val `Extract String` = test {
    source1.string.as[String]
  } returns "Hello"
  
  val `Extract Double` = test {
    source1.double.as[Double]
  } returns 3.14159
  
  val `Extract Boolean` = test {
    source1.boolean.as[Boolean]
  } returns true
  
  val `Extract List[Int]` = test {
    source1.list.as[List[Int]]
  } requires `Extract Int` returns List(1, 2, 3)
  
  val `Extract Vector[Int]` = test {
    source1.list.as[Vector[Int]]
  } requires `Extract Int` returns Vector(1, 2, 3)
  
  val `Extract case class` = test {
    source1.foo.as[Foo]
  } requires (
    `Extract String`,
    `Extract Int`
  ) returns Foo("test", 1)
  
  val `Extract case class with missing optional value` = test {
    source1.baz.as[Baz]
  } returns Baz("test", None)
  
  val `Extract case class with missing tried value` = test {
    source1.baz.as[Baz2]
  } returns Baz2("test", util.Failure(MissingValueException()))
  
  val `Extract case class with present optional value` = test {
    source1.baz2.as[Baz]
  } returns Baz("test", Some(7))
  
  val `Extract case class with present tried value` = test {
    source1.baz2.as[Baz2]
  } returns Baz2("test", util.Success(7))
  
  val `Extract nested case class` = test {
    source1.bar.as[Bar]
  } requires (
    `Extract case class`
  ) returns Bar(Foo("test2", 2), 2.7)
  
  val `Extract deeply-nested case class` = test {
    json"""{ "a": { "b": { "c": { "d": { "e": { "f": 1 } } } } } }""".as[A]
  } returns A(B(C(D(E(F(1))))))

  val `Extract List element` = test {
    source1.list(1).as[Int]
  } requires (
    `Extract Int`
  ) returns 2
  
  val `Extract object element` = test {
    source1.bar.foo.alpha.as[String]
  } requires (
    `Extract String`
  ) returns "test2"

  val `Check type failure` = test {
    source1.string.as[Int]
  } throws TypeMismatchException(DataTypes.String, DataTypes.Number)

  val `Check missing value failure` = test {
    source1.nothing.as[Int]
  } throws MissingValueException()

  val `Match string` = test {
    source1 match {
      case json""" { "string": $h } """ => h.as[String]
    }
  } returns "Hello"

  val `Match inner JSON` = test {
    source1 match {
      case json""" { "foo": $foo } """ => foo
    }
  } returns json"""{ "alpha": "test", "beta": 1 }"""
  
  val `Match inner string` = test {
    source1 match {
      case json""" { "foo": { "alpha": $t } } """ => t.as[String]
    }
  } returns "test"
  
  val `Filtered match` = test {
    source1 match {
      case json""" { "int": 42, "foo": { "alpha": $t } } """ => t.as[String]
    }
  } returns "test"
  
  val `Inner filtered match` = test {
    source1 match {
      case json""" { "foo": { "alpha": "test" }, "bar": { "gamma": $g } } """ => g.as[Double]
    }
  } returns 2.7
  
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
  } returns 2

  val `Empty object doesn't match` = test {
    json"""{ "foo": "bar" }""" match {
      case json"""{ "foo": {} }""" => 0
    }
  } throws classOf[MatchError]

  val `Serialize string` = test {
    Json("Hello World!").toString
  } returns """"Hello World!""""

  val `Serialize int` = test {
    Json(1648).toString
  } returns "1648"

  val `Serialize array` = test {
    Json(List(1, 2, 3)).toString
  } returns "[1,2,3]"

  val `Serialize object` = test {
    import formatters.humanReadable._
    Json.format(json"""{"baz":"quux","foo":"bar"}""")
  } returns """{
             | "baz": "quux",
             | "foo": "bar"
             |}""".stripMargin
  
  val `Empty object serialization` = test {
    import formatters.humanReadable._
    Json.format(json"{}")
  } returns "{}"
  
  val `Empty array serialization` = test {
    import formatters.humanReadable._
    Json.format(json"[]")
  } returns "[]"

  // As reported by Jim Newsham
  val `Extracting Option should not throw exception` = test {
    val j = json"""{"foo":"bar"}"""
    j.as[Option[String]]
  } returns None

  // Reported by @ajrnz
  val `Tabs should be escaped when serializing strings` = test {
    Json("\t").toString
  } returns """"\t""""
}

abstract class MutableJsonTests(ast: JsonBufferAst, parser: Parser[String, JsonBufferAst]) extends TestSuite {
 
  implicit def implicitAst: JsonBufferAst = ast
  implicit def implicitParser: Parser[String, JsonBufferAst] = parser

  case class Foo(alpha: String, beta: Int)
  case class Bar(foo: Foo, gamma: Double)
  
  val mutableSource = jsonBuffer"""{
    "string": "Hello",
    "int": 42,
    "double": 3.14159,
    "boolean": true,
    "list": [1, 2, 3],
    "foo": { "alpha": "test", "beta": 1 },
    "bar": { "foo": { "alpha": "test2", "beta": 2 }, "gamma": 2.7 },
    "baz": { "alpha": "test" },
    "baz2": { "alpha": "test", "beta": 7 },
    "self": 0
  }"""

  val `Mutable extract Int` = test {
    mutableSource.int.as[Int]
  } returns 42
  
  val source2 = JsonBuffer.parse("""{
    "string": "Hello",
    "int": 42
  }""")

  val `Mutable get String` = test {
    source2.string.as[String]
  } returns "Hello"

  //val `Mutable get optional String` = test {
  //  source2.string.as[Option[String]]
  //} returns Some("Hello")

  val `Mutable get Int` = test {
    source2.int.as[Int]
  } returns 42

  val `Mutable change String` = test {
    source2.string = "World"
    source2.string.as[String]
  } returns "World"

  val `Mutable add String` = test {
    source2.inner.newString = "Hello"
    source2.inner.newString.as[String]
  } returns "Hello"
 
  val `Mutable add Json` = test {
    val jb = JsonBuffer.empty
    jb.foo = json"""{ "foo": "bar" }"""
  } returns jsonBuffer"""{ "foo": { "foo": "bar" } }"""

  val `Mutable add case class` = test {
    source2.foo = Foo("string", -1)
    source2.foo.as[Foo]
  } returns Foo("string", -1)
 
  val `Deep insertion of integer` = test {
    source2.alpha.beta.gamma.delta = 1
    source2.alpha.beta.gamma.delta.as[Int]
  } returns 1

  val `Array autopadding` = test {
    source2.autopad(4) = 1
    source2.autopad(4).as[Int]
  } returns 1

  val `Deep array insertion of integer` = test {
    source2.array(1)(2)(3)(4) = 1
    source2.array(1)(2)(3)(4).as[Int]
  } requires `Array autopadding` returns 1

  val `Deep mixed insertion of string` = test {
    source2.mixed(4).foo.bar(2).baz = "Mixed"
    source2.mixed(4).foo.bar(2).baz.as[String]
  } requires `Array autopadding` returns "Mixed"

  val `Mutable add array String` = test {
    source2.inner.newArray += "Hello"
    source2.inner.newArray(0).as[String]
  } returns "Hello"
 
}

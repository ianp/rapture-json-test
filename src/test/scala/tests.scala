package rapture.json.test

import rapture.core._
import rapture.json._
import rapture.data._
//import rapture.test._
import org.scalatest._

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

case class Strings(xs: List[String])
case class Ints(xs: List[Int])
case class Things[T](xs: List[T])

import jsonBackends._
class ScalaJsonTests() extends JsonTests(scalaJson.implicitJsonAst, scalaJson.implicitJsonStringParser)
class PlayTests() extends JsonTests(play.implicitJsonAst, play.implicitJsonStringParser)
class JawnTests() extends JsonTests(jawn.implicitJsonAst, jawn.implicitJsonStringParser(jawn.jawnFacade))
class Json4sTests() extends JsonTests(json4s.implicitJsonAst, json4s.implicitJsonStringParser)
class SprayTests() extends JsonTests(spray.implicitJsonAst, spray.implicitJsonStringParser)
class JacksonTests() extends JsonTests(jackson.implicitJsonAst, jackson.implicitJsonStringParser)
class ArgonautTests() extends JsonTests(argonaut.implicitJsonAst, argonaut.implicitJsonStringParser)
//class LiftTests() extends JsonTests(lift.implicitJsonAst, lift.implicitJsonStringParser)

class MutableScalasonTests() extends MutableJsonTests(scalaJson.implicitJsonAst, scalaJson.implicitJsonStringParser)
class MutablePlayTests() extends MutableJsonTests(play.implicitJsonAst, play.implicitJsonStringParser)
class MutableJawnTests() extends MutableJsonTests(jawn.implicitJsonAst, jawn.implicitJsonStringParser(jawn.jawnFacade))
class MutableJson4sTests() extends MutableJsonTests(json4s.implicitJsonAst, json4s.implicitJsonStringParser)
class MutableSprayTests() extends MutableJsonTests(spray.implicitJsonAst, spray.implicitJsonStringParser)
class MutableArgonautTests() extends MutableJsonTests(argonaut.implicitJsonAst, argonaut.implicitJsonStringParser)
//class MutableLiftTests() extends MutableJsonTests(lift.implicitJsonAst, lift.implicitJsonStringParser)

abstract class JsonTests(ast: JsonAst, parser: Parser[String, JsonAst]) extends FlatSpec with Matchers {

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
    "self": 0,
    "strings": { "xs": ["a", "beginning", "is", "a", "very", "delicate", "time", "..."] },
    "ints": { "xs": [0, 1, 1, 2, 3, 5, 8, 13] }
  }"""

  "json" should "extract int" in {
    source1.int.as[Int] shouldBe 42
  }

  it should "extract value called self" in {
    source1.self.as[Int] shouldBe 0
  }

  it should "Extract Option[Int]" in {
    source1.int.as[Option[Int]] shouldBe Some(42)
  }
  
  it should "Extract Option[Int], wrong type" in {
    source1.string.as[Option[Int]] shouldBe None
  }
 
  it should "Extract String" in {
    source1.string.as[String] shouldBe "Hello"
  }
  
  it should "Extract Double" in {
    source1.double.as[Double] shouldBe 3.14159
  }
  
  it should "Extract Boolean" in {
    source1.boolean.as[Boolean] shouldBe true
  }
  
  it should "Extract List[Int]" in {
    source1.list.as[List[Int]] shouldBe List(1, 2, 3)
  }
  
  it should "Extract Vector[Int]" in {
    source1.list.as[Vector[Int]] shouldBe Vector(1, 2, 3)
  }
  
  it should "Extract case class" in {
    source1.foo.as[Foo] shouldBe Foo("test", 1)
  }
  
  it should "Extract case class with missing optional value" in {
    source1.baz.as[Baz] shouldBe Baz("test", None)
  }
  
  it should "Extract case class with missing tried value" in {
    source1.baz.as[Baz2] shouldBe Baz2("test", util.Failure(MissingValueException()))
  }
  
  it should "Extract case class with present optional value" in {
    source1.baz2.as[Baz] shouldBe Baz("test", Some(7))
  }
  
  it should "Extract case class with present tried value" in {
    source1.baz2.as[Baz2] shouldBe Baz2("test", util.Success(7))
  }
  
  it should "Extract nested case class" in {
    source1.bar.as[Bar] shouldBe Bar(Foo("test2", 2), 2.7)
  }
  
  //val `Extract deeply-nested case class` = test {
  //  json"""{ "a": { "b": { "c": { "d": { "e": { "f": 1 } } } } } }""".as[A]
  //} returns A(B(C(D(E(F(1))))))

  it should "Extract List element" in {
    source1.list(1).as[Int] shouldBe 2
  }
  
  it should "Extract object element" in {
    source1.bar.foo.alpha.as[String] shouldBe "test2"
  }

  it should "Check type failure" in {
    a[TypeMismatchException] should be thrownBy source1.string.as[Int]
  }

  it should "Check missing value failure" in {
    a[MissingValueException] should be thrownBy source1.nothing.as[Int]
  }

  it should "Match string" in {
    (source1 match {
      case json""" { "string": $h } """ => h.as[String]
    }) shouldBe "Hello"
  }

  it should "Match inner JSON" in {
    (source1 match {
      case json""" { "foo": $foo } """ => foo
    }) shouldBe json"""{ "alpha": "test", "beta": 1 }"""
  }
  
  it should "Match inner string" in {
    (source1 match {
      case json""" { "foo": { "alpha": $t } } """ => t.as[String]
    }) shouldBe "test"
  }
  
  it should "Filtered match" in {
    (source1 match {
      case json""" { "int": 42, "foo": { "alpha": $t } } """ => t.as[String]
    }) shouldBe "test"
  }
  
  it should "Inner filtered match" in {
    (source1 match {
      case json""" { "foo": { "alpha": "test" }, "bar": { "gamma": $g } } """ => g.as[Double]
    }) shouldBe 2.7
  }
  
  it should "Filtered failed match" in {
    a[MatchError] should be thrownBy (source1 match {
      case json""" { "int": 0, "foo": { "alpha": $t } } """ => t.as[String]
    })
  }

  it should "Multiple pattern match" in {
    (json"""{ "foo": "bar" }""" match {
      case json"""{ "bar": "foo" }""" => 0
      case json"""{ "foo": "baz" }""" => 1
      case json"""{ "foo": "bar" }""" => 2
    }) shouldBe 2
  }

  it should "Empty object doesn't match" in {
    a[MatchError] should be thrownBy (json"""{ "foo": "bar" }""" match {
      case json"""{ "foo": {} }""" => 0
    })
  }

  it should "Serialize string" in {
    Json("Hello World!").toString shouldBe """"Hello World!""""
  }

  it should "Serialize int" in {
    Json(1648).toString shouldBe "1648"
  }

  it should "Serialize array" in {
    Json(List(1, 2, 3)).toString shouldBe "[1,2,3]"
  }

  it should "Serialize object" in {
    import formatters.humanReadable._
    Json.format(json"""{"baz":"quux","foo":"bar"}""") shouldBe """{
                                                                 | "baz": "quux",
                                                                 | "foo": "bar"
                                                                 |}""".stripMargin
  }
  
  it should "Empty object serialization" in {
    import formatters.humanReadable._
    Json.format(json"{}") shouldBe "{}"
  }
  
  it should "Empty array serialization" in {
    import formatters.humanReadable._
    Json.format(json"[]") shouldBe "[]"
  }

  // As reported by Jim Newsham
  it should "Extracting Option should not throw exception" in {
    val j = json"""{"foo":"bar"}"""
    j.as[Option[String]] shouldBe None
  }

  it should "Extract sequences" in {
    source1.strings.as[Strings] shouldBe Strings(List("a", "beginning", "is", "a", "very", "delicate", "time", "..."))
    source1.ints.as[Ints] shouldBe Ints(List(0, 1, 1, 2, 3, 5, 8, 13))
  }

  it should "Extract parameterised types" in {
    source1.strings.as[Things[String]] shouldBe Things(List("a", "beginning", "is", "a", "very", "delicate", "time", "..."))
    source1.ints.as[Things[Int]] shouldBe Things(List(0, 1, 1, 2, 3, 5, 8, 13))
  }

}

abstract class MutableJsonTests(ast: JsonBufferAst, parser: Parser[String, JsonBufferAst]) extends FlatSpec with Matchers {
 
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

  "mutable json" should "Mutable extract Int" in {
    mutableSource.int.as[Int] shouldBe 42
  }
  
  val source2 = JsonBuffer.parse("""{
    "string": "Hello",
    "int": 42
  }""")

  it should "Mutable get String" in {
    source2.string.as[String] shouldBe "Hello"
  }

  //it should "Mutable get optional String" in {
  //  source2.string.as[Option[String]]
  //} shouldBe Some("Hello")

  it should "Mutable get Int" in {
    source2.int.as[Int] shouldBe 42
  }

  it should "Mutable change String" in {
    source2.string = "World"
    source2.string.as[String] shouldBe "World"
  }

  it should "Mutable add String" in {
    source2.inner.newString = "Hello"
    source2.inner.newString.as[String] shouldBe "Hello"
  }
 
  it should "Mutable add Json" in {
    val jb = JsonBuffer.empty
    jb.foo = json"""{ "foo": "bar" }"""
    jb shouldBe jsonBuffer"""{ "foo": { "foo": "bar" } }"""
  }

  it should "Mutable add case class" in {
    source2.foo = Foo("string", -1)
    source2.foo.as[Foo] shouldBe Foo("string", -1)
  }
 
  it should "Deep insertion of integer" in {
    source2.alpha.beta.gamma.delta = 1
    source2.alpha.beta.gamma.delta.as[Int] shouldBe 1
  }

  it should "Array autopadding" in {
    source2.autopad(4) = 1
    source2.autopad(4).as[Int] shouldBe 1
  }

  it should "Deep array insertion of integer" in {
    source2.array(1)(2)(3)(4) = 1
    source2.array(1)(2)(3)(4).as[Int] shouldBe 1
  }

  it should "Deep mixed insertion of string" in {
    source2.mixed(4).foo.bar(2).baz = "Mixed"
    source2.mixed(4).foo.bar(2).baz.as[String] shouldBe "Mixed"
  }

  it should "Mutable add array String" in {
    source2.inner.newArray += "Hello"
    source2.inner.newArray(0).as[String] shouldBe "Hello"
  }
 
}

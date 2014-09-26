object project extends ProjectSettings {
  def scalaVersion = "2.10.4"
  def version = "1.0.0"
  def name = "json-test"
  def description = "Tests for Rapture JSON"
  
  def dependencies = Seq(
    "core" -> "0.10.0",
    "test" -> "0.10.0",
    "data" -> "0.10.1",
    "json" -> "0.10.1",
    "json-jawn" -> "0.10.0",
    "json-lift" -> "0.10.0",
    "json-jackson" -> "0.10.0",
    "json-json4s" -> "0.10.0",
    "json-spray" -> "0.10.0",
    "json-argonaut" -> "0.10.0"
  )
  
  def thirdPartyDependencies = Seq(
    ("org.json4s", "json4s-native_2.10", "3.2.9"),
    ("org.spire-math", "jawn-ast_2.10", "0.6.0"),
    ("org.spire-math", "jawn-parser_2.10", "0.6.0")
  )

  def imports = Seq(
    "rapture.core._",
    "rapture.json._",
    "rapture.data._",
    "rapture.test._"
  )

  override def mainClass = "rapture.test.Main"
}

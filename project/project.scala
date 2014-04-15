object project extends ProjectSettings {
  def scalaVersion = "2.10.4"
  def version = "0.9.0"
  def name = "json-test"
  def description = "Tests for Rapture JSON"
  
  def dependencies = Seq(
    "core" -> "0.9.0",
    "test" -> "0.9.0",
    "json" -> "0.9.1",
    "json-jawn" -> "0.9.0",
    "json-jackson" -> "0.9.0"
  )
  
  def thirdPartyDependencies = Nil

  def imports = Seq(
    "rapture.core._",
    "rapture.json._",
    "jsonParsers.scalaJson._",
    "strategy.throwExceptions"
  )

  override def mainClass = "rapture.test.Main"
}

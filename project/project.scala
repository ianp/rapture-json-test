object project extends ProjectSettings {
  def scalaVersion = "2.11.2"
  def version = "1.0.4"
  def name = "json-test"
  def description = "Tests for Rapture JSON"
  
  def dependencies = Seq(
    "test" -> "0.10.1",
    "json-jawn" -> "1.0.4",
    "json-jackson" -> "1.0.4",
    "json-json4s" -> "1.0.4",
    "json-spray" -> "1.0.4",
    "json-argonaut" -> "1.0.4"
  )
  
  def thirdPartyDependencies = Seq(
  )

  def imports = Seq(
    "rapture.core._",
    "rapture.json._",
    "rapture.data._",
    "rapture.test._"
  )

  override def mainClass = "rapture.test.Main"
}

object project extends ProjectSettings {
  def scalaVersion = "2.10.4"
  def version = "1.0.8"
  def name = "json-test"
  def description = "Tests for Rapture JSON"
  
  def dependencies = Seq(
    "test" -> "0.10.1",
    "json-argonaut" -> "1.0.8",
    "json-jackson" -> "1.0.8",
    "json-jawn" -> "1.0.8",
    "json-json4s" -> "1.0.8",
    "json-spray" -> "1.0.8",
    "json-play" -> "1.0.8",
    "json" -> "1.0.8",
    "log" -> "0.1.0"
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

object project extends ProjectSettings {
  def scalaVersion = "2.11.6"
  def version = "1.1.0"
  def name = "json-test"
  def description = "Tests for Rapture JSON"
  
  def dependencies = Seq(
<<<<<<< HEAD
    "test" -> "0.10.1",
    "json-jawn" -> "1.0.4",
    "json-jackson" -> "1.0.4",
    "json-json4s" -> "1.0.4",
    "json-spray" -> "1.0.4",
    "json-argonaut" -> "1.0.4"
=======
    "test" -> "1.1.0",
    "json-argonaut" -> "1.1.0",
    "json-jackson" -> "1.1.0",
    "json-jawn" -> "1.1.0",
    //"json-lift" -> "1.1.0",
    "json-json4s" -> "1.1.0",
    "json-spray" -> "1.1.0",
    "json-play" -> "1.1.0",
    "json" -> "1.1.0"
//    "log" -> "0.1.0"
>>>>>>> master
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

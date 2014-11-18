package rapture.json.test

import rapture.test._
import rapture.log._

object Test {
  implicit val rep = new CmdLineReporter(120)

  //import basicStdoutLogger._

  new jawn.JsonTests().runAll()
  //new jackson.JsonTests().runAll()
  new json4s.JsonTests().runAll()
  new play.JsonTests().runAll()
  new scalaJson.JsonTests().runAll()
  new argonaut.JsonTests().runAll()
  new spray.JsonTests().runAll()
  
  new jawn.MutableJsonTests().runAll()
  new json4s.MutableJsonTests().runAll()
  new play.MutableJsonTests().runAll()
  new scalaJson.MutableJsonTests().runAll()
  new argonaut.MutableJsonTests().runAll()
  new spray.MutableJsonTests().runAll()

  //log.info("Here's an info message")
  //log.debug("And here's a debug one.")
}

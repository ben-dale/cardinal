package uk.co.ridentbyte.model

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.writePretty

case class Config(environmentVariables: List[EnvironmentVariable]) {

  private implicit val formats: DefaultFormats = DefaultFormats

  def toJson: String = writePretty(this)

  def withEnvironmentVariables(environmentVariables: List[EnvironmentVariable]): Config = Config(environmentVariables)

  def getEnvironmentVariables: List[EnvironmentVariable] = environmentVariables
}

object Config {
  private implicit val formats: DefaultFormats = DefaultFormats
  def apply(json: String): Config =
    try {
      parse(json).extract[Config]
    } catch {
      case _: Exception => Config(List.empty[EnvironmentVariable])
    }
}

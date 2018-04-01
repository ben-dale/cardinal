package uk.co.ridentbyte.model

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.writePretty

case class Config(environmentVariables: List[String]) {
  private implicit val formats: DefaultFormats = DefaultFormats

  def toJson: String = writePretty(this)

  def withEnvironmentVariables(environmentVariables: List[String]): Config = Config(environmentVariables)

  def getEnvironmentVariables: Map[String, String] = {
    environmentVariables.filter(_.contains("=")).map { variable =>
      val splitVar = variable.split("=")
      (splitVar(0), splitVar(1))
    }.toMap
  }
}

object Config {
  private implicit val formats: DefaultFormats = DefaultFormats
  def apply(json: String): Config = parse(json).extract[Config]
}

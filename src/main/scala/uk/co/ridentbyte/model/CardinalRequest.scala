package uk.co.ridentbyte.model

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.writePretty
import uk.co.ridentbyte.Cardinal

object CardinalRequest {
  private implicit val formats: DefaultFormats = DefaultFormats
  def apply(json: String): CardinalRequest = parse(json).extract[CardinalRequest]
}

case class CardinalRequest(uri: String, verb: String, headers: List[String], body: Option[String]) {
  private implicit val formats: DefaultFormats = DefaultFormats

  def toJson: String = writePretty(this)

  def withId(id: String): CardinalRequest = {
    CardinalRequest(
      uri.replaceAll("#\\{id\\}", id),
      verb,
      headers.map(_.replaceAll("#\\{id\\}", id)),
      if (body.isDefined) Some(body.get.replaceAll("#\\{id\\}", id)) else None
    )
  }

  def processConstants(config: Config): CardinalRequest = {
    val vars = config.getEnvironmentVariables
    val newUri = RequestString(uri, vars, Cardinal.vocabulary).process
    val newHeaders = headers.map(h => RequestString(h, vars, Cardinal.vocabulary).process)
    val newBody = body match {
      case Some(b) => Some(RequestString(b, vars, Cardinal.vocabulary).process)
      case _ => None
    }
    CardinalRequest(newUri, verb, newHeaders, newBody)
  }

  def toCurl(config: Config): String = {
    Curl(uri, verb, body, headers, config.getEnvironmentVariables).toCommand
  }

}
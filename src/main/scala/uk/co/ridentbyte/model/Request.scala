package uk.co.ridentbyte.model

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.writePretty

case class Request(uri: String, verb: String, headers: List[String], body: Option[String]) {
  private implicit val formats: DefaultFormats = DefaultFormats

  def toJson: String = writePretty(this)

  def withId(id: String): Request = {
    Request(
      uri.replaceAll("#\\{id\\}", id),
      verb,
      headers.map(_.replaceAll("#\\{id\\}", id)),
      if (body.isDefined) Some(body.get.replaceAll("#\\{id\\}", id)) else None
    )
  }

  def processEnvironmentVariables(value: String, vars: Map[String, String]): String = {
    var valueCopy = value
    vars.foreach { case (k, v) => valueCopy = valueCopy.replaceAll("#\\{" + k + "\\}", v) }
    valueCopy
  }

  def processConstants(config: Config): Request = {
    val vars = config.getEnvironmentVariables
    val newUri = RequestString(uri, vars).process
    val newHeaders = headers.map(h => RequestString(h, vars).process)
    val newBody = body match {
      case Some(b) => Some(RequestString(b, vars).process)
      case _  => None
    }
    Request(newUri, verb, newHeaders, newBody)
  }

  def toCurl(config: Config): String = {
    val vars = config.getEnvironmentVariables
    val sb = new StringBuilder
    sb.append("curl ")
    headers.foreach { header => sb.append(s"""-H '${RequestString(header, vars).process}' """) }
    body.foreach { b => sb.append(s"""-d '${RequestString(b, vars).process}' """) }
    sb.append(s"""-X $verb ${RequestString(uri, vars).process}""")
    sb.toString()
  }

}

object Request {
  private implicit val formats: DefaultFormats = DefaultFormats
  def apply(json: String): Request = parse(json).extract[Request]
}
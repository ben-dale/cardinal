package uk.co.ridentbyte.model

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.writePretty

case class Request(name: Option[String], uri: String, verb: String, headers: List[String], body: Option[String]) {
  private implicit val formats: DefaultFormats = DefaultFormats
  def withName(name: String): Request = Request(Some(name), uri, verb, headers, body)
  def toJson: String = writePretty(this)
}

object Request {
  private implicit val formats: DefaultFormats = DefaultFormats
  def apply(json: String): Request = parse(json).extract[Request]
}
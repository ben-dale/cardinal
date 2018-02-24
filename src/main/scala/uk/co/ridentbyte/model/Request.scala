package uk.co.ridentbyte.model

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.write

case class Request(uri: String, verb: String, headers: List[String], body: Option[String]) {
  private implicit val formats: DefaultFormats = DefaultFormats
  def toJson: String = write(this)
}

object Request {
  private implicit val formats: DefaultFormats = DefaultFormats
  def apply(json: String): Request = parse(json).extract[Request]
}
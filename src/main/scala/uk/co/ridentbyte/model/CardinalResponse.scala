package uk.co.ridentbyte.model

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.writePretty
import scala.collection.JavaConverters._

class CardinalResponse(val raw: CardinalHttpResponse, val time: Long) {
  private implicit val formats: DefaultFormats = DefaultFormats

  def formattedBody: String = {
    val contentType = raw.getHeaders.asScala.get("Content-Type")
    contentType match {
      case Some(ct) => ct match {
        case _: String if ct.contains("application/json") =>
          try { writePretty(parse(raw.getBody)) } catch { case _: Exception => raw.getBody }
        case _ => raw.getBody
      }
      case _ => raw.getBody
    }
  }

  def toCSV: String = {
    s""""${raw.getStatusCode}","${raw.getHeaders.asScala.map(h => h._1 + ":" + h._2.head).mkString("\n").replace("\"", "\"\"")}","${raw.getBody.replace("\"", "\"\"")}","$time""""
  }
}

case class BlankCardinalResponse() extends CardinalResponse(new CardinalHttpResponse("", Map.empty[String, String].asJava, 0), 0L) {
  override def toCSV: String = ",,,"
  override def formattedBody: String = ""
}

object CardinalResponse {
  def csvHeaders: String = {
    "response code,response headers,response body,response time (ms)"
  }
}

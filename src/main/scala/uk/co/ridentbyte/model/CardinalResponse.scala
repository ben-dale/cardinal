package uk.co.ridentbyte.model

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.writePretty
import scalaj.http.HttpResponse

class CardinalResponse(val raw: HttpResponse[String], val time: Long) {
  private implicit val formats: DefaultFormats = DefaultFormats

  def formattedBody: String = {
    val contentType = raw.header("Content-Type")
    contentType match {
      case Some(ct) => ct match {
        case _: String if ct.contains("application/json") =>
          try { writePretty(parse(raw.body)) } catch { case _: Exception => raw.body }
        case _ => raw.body
      }
      case _ => raw.body
    }
  }

  def toCSV: String = {
    s""""${raw.code}","${raw.headers.map(h => h._1 + ":" + h._2.head).mkString("\n").replace("\"", "\"\"")}","${raw.body.replace("\"", "\"\"")}","$time""""
  }
}

case class BlankCardinalResponse() extends CardinalResponse(HttpResponse.apply("", 0, Map.empty), 0L) {
  override def toCSV: String = ",,,"
  override def formattedBody: String = ""
}

object CardinalResponse {
  def csvHeaders: String = {
    "response code,response headers,response body,response time (ms)"
  }
}

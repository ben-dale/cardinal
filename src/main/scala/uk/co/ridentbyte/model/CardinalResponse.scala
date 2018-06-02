package uk.co.ridentbyte.model

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.writePretty
import scalaj.http.HttpResponse

case class CardinalResponse(raw: HttpResponse[String], time: Long) {
  private implicit val formats: DefaultFormats = DefaultFormats

  def formattedBody: String = {
    val contentType = raw.header("Content-Type")
    contentType match {
      case Some(ct) => ct match {
        case _: String if ct.contains("application/json") => writePretty(parse(raw.body))
        case _ => raw.body
      }
      case _ => raw.body
    }
  }

  def toCSV: String = {
    s""""${raw.code}","${raw.headers.map(h => h._1 + ":" + h._2.head).mkString("\n").replace("\"", "\"\"")}","${raw.body.replace("\"", "\"\"")}","$time""""
  }
}

object CardinalResponse {
  def csvHeaders: String = {
    "responseCode,responseHeaders,responseBody,responseTime (ms)"
  }
}

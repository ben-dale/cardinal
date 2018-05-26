package uk.co.ridentbyte.model

import org.json4s.DefaultFormats

import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.writePretty

case class HttpResponseWrapper(httpResponse: scalaj.http.HttpResponse[String], time: Long) {
  private implicit val formats: DefaultFormats = DefaultFormats

  def formattedBody: String = {
    val contentType = httpResponse.header("Content-Type")
    contentType match {
      case Some(ct) => ct match {
        case _: String if ct.contains("application/json") => writePretty(parse(httpResponse.body))
        case _ => httpResponse.body
      }
      case _ => httpResponse.body
    }

  }
}

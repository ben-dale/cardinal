package uk.co.ridentbyte.model

import scalaj.http.HttpResponse

case class HttpResponseWrapper(httpResponse: HttpResponse[String], time: Long)

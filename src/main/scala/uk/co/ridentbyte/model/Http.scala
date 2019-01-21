package uk.co.ridentbyte.model

import java.net.{MalformedURLException, URI, URL, URLDecoder}

case class Http(request: CardinalRequest) {

  private def parseURI(rawUri: String): String = {
    val rawUriWithProtocol = try {
      new URL(rawUri)
      new URI(rawUri)
      rawUri
    } catch {
      case _: MalformedURLException => "http://" + rawUri
    }
    val decodedURL = URLDecoder.decode(rawUriWithProtocol, "UTF-8")
    val url = new URL(decodedURL)
    val uri = new URI(url.getProtocol, url.getUserInfo, url.getHost, url.getPort, url.getPath, url.getQuery, url.getRef)
    uri.toASCIIString
  }

  def send: scalaj.http.HttpResponse[String] = {
    val parsedUri = parseURI(request.uri)
    val splitHeaders = request.headers.map { header =>
      val splitHeader = header.split(":")
      if (splitHeader.length == 2) {
        (splitHeader(0), splitHeader(1))
      } else {
        (splitHeader(0), "")
      }
    }
    
    request.verb match {
      case "POST" if request.body.isDefined => {
        scalaj.http.Http(parsedUri)
          .option(scalaj.http.HttpOptions.followRedirects(true))
          .copy(headers = splitHeaders)
          .postData(request.body.get)
          .asString
      }
      case "PUT" if request.body.isDefined => {
        scalaj.http.Http(parsedUri)
          .option(scalaj.http.HttpOptions.followRedirects(true))
          .copy(headers = splitHeaders)
          .put(request.body.get)
          .asString
      }
      case _ => {
        scalaj.http.Http(parsedUri)
          .option(scalaj.http.HttpOptions.followRedirects(true))
          .method(request.verb)
          .copy(headers = splitHeaders)
          .asString
      }
    }

  }

}

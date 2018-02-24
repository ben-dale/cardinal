package uk.co.ridentbyte.util

import java.net._

import uk.co.ridentbyte.model.Request

import scalaj.http.{Http, HttpOptions, HttpResponse}

object HttpUtil {

  def parseURI(rawUri: String): String = {
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

  def sendRequest(request: Request): HttpResponse[String] = {
    val parsedUri = HttpUtil.parseURI(request.uri)
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
        println("here with " + request.body.get)
        Http(parsedUri).option(HttpOptions.followRedirects(true)).method(request.verb).headers(splitHeaders).postData(request.body.get).asString
      }
      case _ => {
        Http(parsedUri).option(HttpOptions.followRedirects(true)).method(request.verb).headers(splitHeaders).asString
      }
    }


  }

}

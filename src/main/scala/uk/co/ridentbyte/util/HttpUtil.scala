package uk.co.ridentbyte.util

import java.net._

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

  def sendRequest(uri: String, verb: String, headers: List[String], body: Option[String]): HttpResponse[String] = {
    val splitHeaders = headers.map { header =>
      val splitHeader = header.split(":")
      if (splitHeader.length == 2) {
        (splitHeader(0), splitHeader(1))
      } else {
        (splitHeader(0), "")
      }
    }

    verb match {
      case "POST" if body.isDefined => {
        println("here with " + body.get)
        Http(uri).option(HttpOptions.followRedirects(true)).method(verb).headers(splitHeaders).postData(body.get).asString
      }
      case _ => {
        Http(uri).option(HttpOptions.followRedirects(true)).method(verb).headers(splitHeaders).asString
      }
    }


  }

}

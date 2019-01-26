package uk.co.ridentbyte.model

import java.net.{MalformedURLException, URI, URL, URLDecoder}

import org.apache.http.client.methods._
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{HttpClientBuilder, LaxRedirectStrategy}
import org.apache.http.util.EntityUtils

import scala.collection.JavaConverters._

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

  def send: CardinalHttpResponse = {
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
      case "POST" => {

        val httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build()

        val httpPost = new HttpPost(parsedUri)
        splitHeaders.foreach { header =>
          httpPost.setHeader(header._1, header._2)
        }
        if (request.body.isDefined) {
          httpPost.setEntity(new StringEntity(request.body.get))
        }

        val res = httpClient.execute(httpPost)
        val headers = res.getAllHeaders.map(h => (h.getName, h.getValue)).toMap.asJava
        val body = if (res.getEntity != null ) EntityUtils.toString(res.getEntity) else ""
        val code = res.getStatusLine.getStatusCode

        new CardinalHttpResponse(body, headers, code)
      }

      case "PUT" => {
        val httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build()

        val httpPut = new HttpPut(parsedUri)
        splitHeaders.foreach { header =>
          httpPut.setHeader(header._1, header._2)
        }

        if (request.body.isDefined) {
          httpPut.setEntity(new StringEntity(request.body.get))
        }

        val res = httpClient.execute(httpPut)
        val headers = res.getAllHeaders.map(h => (h.getName, h.getValue)).toMap.asJava
        val body = if (res.getEntity != null ) EntityUtils.toString(res.getEntity) else ""
        val code = res.getStatusLine.getStatusCode

        new CardinalHttpResponse(body, headers, code)
      }

      case "GET" => {
        val httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build()

        val httpGet = new HttpGet(parsedUri)
        splitHeaders.foreach { header =>
          httpGet.setHeader(header._1, header._2)
        }

        val res = httpClient.execute(httpGet)
        val headers = res.getAllHeaders.map(h => (h.getName, h.getValue)).toMap.asJava
        val body = if (res.getEntity != null ) EntityUtils.toString(res.getEntity) else ""
        val code = res.getStatusLine.getStatusCode

        new CardinalHttpResponse(body, headers, code)
      }

      case "DELETE" => {
        val httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build()

        val httpDelete = new HttpDelete(parsedUri)
        splitHeaders.foreach { header =>
          httpDelete.setHeader(header._1, header._2)
        }

        val res = httpClient.execute(httpDelete)
        val headers = res.getAllHeaders.map(h => (h.getName, h.getValue)).toMap.asJava
        val body = if (res.getEntity != null ) EntityUtils.toString(res.getEntity) else ""
        val code = res.getStatusLine.getStatusCode

        new CardinalHttpResponse(body, headers, code)
      }

      case "HEAD" => {
        val httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build()

        val httpHead = new HttpHead(parsedUri)
        splitHeaders.foreach { header =>
          httpHead.setHeader(header._1, header._2)
        }

        val res = httpClient.execute(httpHead)
        val headers = res.getAllHeaders.map(h => (h.getName, h.getValue)).toMap.asJava
        val body = if (res.getEntity != null ) EntityUtils.toString(res.getEntity) else ""
        val code = res.getStatusLine.getStatusCode

        new CardinalHttpResponse(body, headers, code)
      }

      case "OPTIONS" => {
        val httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build()

        val httpOptions = new HttpOptions(parsedUri)
        splitHeaders.foreach { header =>
          httpOptions.setHeader(header._1, header._2)
        }

        val res = httpClient.execute(httpOptions)
        val headers = res.getAllHeaders.map(h => (h.getName, h.getValue)).toMap.asJava
        val body = if (res.getEntity != null ) EntityUtils.toString(res.getEntity) else ""
        val code = res.getStatusLine.getStatusCode

        new CardinalHttpResponse(body, headers, code)
      }

      case "TRACE" => {
        val httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build()

        val httpTrace = new HttpTrace(parsedUri)
        splitHeaders.foreach { header =>
          httpTrace.setHeader(header._1, header._2)
        }

        val res = httpClient.execute(httpTrace)
        val headers = res.getAllHeaders.map(h => (h.getName, h.getValue)).toMap.asJava
        val body = if (res.getEntity != null ) EntityUtils.toString(res.getEntity) else ""
        val code = res.getStatusLine.getStatusCode

        new CardinalHttpResponse(body, headers, code)
      }

      case "PATCH" => {
        val httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build()

        val httpPatch = new HttpPatch(parsedUri)
        splitHeaders.foreach { header =>
          httpPatch.setHeader(header._1, header._2)
        }

        if (request.body.isDefined) {
          httpPatch.setEntity(new StringEntity(request.body.get))
        }

        val res = httpClient.execute(httpPatch)
        val headers = res.getAllHeaders.map(h => (h.getName, h.getValue)).toMap.asJava
        val body = if (res.getEntity != null ) EntityUtils.toString(res.getEntity) else ""
        val code = res.getStatusLine.getStatusCode

        new CardinalHttpResponse(body, headers, code)
      }

    }

  }

}

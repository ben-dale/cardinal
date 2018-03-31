package uk.co.ridentbyte.view.request

import javafx.geometry.Insets
import javafx.scene.layout._

import uk.co.ridentbyte.model.Request

class RequestInputPane() extends GridPane {

  private var currentRequest: Option[Request] = None

  setHgap(10)
  setVgap(10)
  setPadding(new Insets(20))

  private val uriVerbInputPane = new RequestUriVerbInputPane
  GridPane.setVgrow(uriVerbInputPane, Priority.NEVER)
  GridPane.setHgrow(uriVerbInputPane, Priority.ALWAYS)
  add(uriVerbInputPane, 0, 0)

  private val headersBodyInputPane = new RequestHeadersBodyInputPane
  GridPane.setVgrow(headersBodyInputPane, Priority.ALWAYS)
  GridPane.setHgrow(headersBodyInputPane, Priority.ALWAYS)
  add(headersBodyInputPane, 0, 1)

  def clear(): Unit = {
    currentRequest = None
    uriVerbInputPane.clear()
    headersBodyInputPane.clear()
  }

  def loadRequest(request: Request): Unit = {
    currentRequest = Some(request)
    headersBodyInputPane.setBody(request.body)
    headersBodyInputPane.setHeaders(request.headers)
    uriVerbInputPane.setVerb(request.verb)
    uriVerbInputPane.setUri(request.uri)
  }

  def getRequest: Request = {
    val body = headersBodyInputPane.getBody
    val headers = headersBodyInputPane.getHeaders
    val verb = uriVerbInputPane.getVerb
    val uri = uriVerbInputPane.getUri
    Request(uri, verb, headers, body)
  }

  def addHeader(header: String): Unit = {
    if (!headersBodyInputPane.getHeaders.contains(header)) {
      headersBodyInputPane.addHeader(header)
    }
  }

  def setBody(body: String): Unit = {
    headersBodyInputPane.setBody(Some(body))
  }

}

package uk.co.ridentbyte.view.request

import javafx.scene.layout._

import uk.co.ridentbyte.model.Request

class RequestPane(sendRequestCallback: (Request) => Unit, clearAllCallback: () => Unit, saveCallback: (Request) => Unit) extends GridPane {

  setStyle(
    """
      |-fx-border-width: 0 1 0 0;
      |-fx-border-color: #DDDDDD;
      |-fx-border-style: hidden solid hidden hidden;
    """.stripMargin)

  val requestInputPane = new RequestInputPane
  GridPane.setHgrow(requestInputPane, Priority.ALWAYS)
  GridPane.setVgrow(requestInputPane, Priority.ALWAYS)
  add(requestInputPane, 0, 0)

  val requestControlPane = new RequestControlPane(sendRequest, clearAllCallback, save)
  GridPane.setVgrow(requestControlPane, Priority.NEVER)
  GridPane.setHgrow(requestControlPane, Priority.ALWAYS)
  add(requestControlPane, 0, 1)

  def sendRequest(): Unit = {
    val verb = requestInputPane.getVerb
    val uri = requestInputPane.getUri
    val headers = requestInputPane.getHeaders
    val body = requestInputPane.getBody
    val request = Request(None, uri, verb, headers, body)
    sendRequestCallback(request)
  }

  def save(): Unit = {
    val verb = requestInputPane.getVerb
    val uri = requestInputPane.getUri
    val headers = requestInputPane.getHeaders
    val body = requestInputPane.getBody
    val filename = requestInputPane.getFilename
    val optFilename = if (filename.length == 0) None else Some(filename)
    val request = Request(optFilename, uri, verb, headers, body)
    saveCallback(request)
  }

  def clear(): Unit = {
    requestInputPane.clear()
  }

  def load(request: Request): Unit = {
    clear()
    requestInputPane.setFilename(request.name.getOrElse(""))
    requestInputPane.addHeaders(request.headers)
    requestInputPane.setUri(request.uri)
    requestInputPane.setVerb(request.verb)
    requestInputPane.setBody(request.body)
  }

  def hasUnsavedChanges: Boolean = {
    requestInputPane.hasUnsavedChanges
  }

  def setUnsavedChanges(unsavedChanges: Boolean): Unit = {
    requestInputPane.setUnsavedChanges(unsavedChanges)
  }

}

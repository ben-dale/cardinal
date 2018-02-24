package uk.co.ridentbyte.view.request

import javafx.scene.control._
import javafx.scene.layout._

class RequestPane(sendRequestCallback: (String, String, List[String], Option[String]) => Unit, clearAllCallback: () => Unit) extends GridPane {

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

  val requestControlPane = new RequestControlPane(sendRequest, showAddHeader, clearAllCallback)
  GridPane.setVgrow(requestControlPane, Priority.NEVER)
  GridPane.setHgrow(requestControlPane, Priority.ALWAYS)
  add(requestControlPane, 0, 1)

  def sendRequest(): Unit = {
    val verb = requestInputPane.getVerb
    val uri = requestInputPane.getUri
    val headers = requestInputPane.getHeaders
    val body = requestInputPane.getBody
    sendRequestCallback(verb, uri, headers, if (body.length != 0) Some(body) else None)
  }

  def showAddHeader(): Unit = {
    val dialog = new TextInputDialog
    dialog.setTitle("New header")
    dialog.setHeaderText("New header")
    dialog.setContentText("Header")
    val result = dialog.showAndWait()
    result.ifPresent((v) => if (v.trim.length > 0) requestInputPane.addHeader(v.trim))
  }

  def clear(): Unit = {
    requestInputPane.clear()
  }
}

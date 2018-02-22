package uk.co.ridentbyte.view

import java.net._
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.geometry.{Insets, VPos}
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, Button, ChoiceBox, TextField}
import javafx.scene.layout.GridPane

import uk.co.ridentbyte.model.Header

import scalaj.http.{Http, HttpOptions, HttpResponse}

class RequestPane(responseCallback: (Iterable[Header], String) => Unit) extends GridPane {

  setGridLinesVisible(true)
  setHgap(10)
  setVgap(10)
  setPadding(new Insets(10, 10, 10, 10))

  val requestUriInput = new TextField()
  requestUriInput.setPromptText("http://localhost:8080")
  requestUriInput.setText("https://google.com")

  val requestVerbSelect = new ChoiceBox[String](FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE", "HEAD", "CONNECT", "OPTIONS", "TRACE", "PATCH"))
  requestVerbSelect.getSelectionModel.selectFirst()

  val sendRequestButton = new Button("Send Request")

  add(requestUriInput, 0, 0)
  add(requestVerbSelect, 1, 0)

  sendRequestButton.setOnAction(sendRequestAction)
  GridPane.setValignment(sendRequestButton, VPos.TOP)
  add(sendRequestButton, 1, 1)

  def sendRequestAction(actionEvent: ActionEvent): Unit = {
    val verb = requestVerbSelect.getSelectionModel.getSelectedItem
    val (uri, hasError) = parseURI(requestUriInput.getText.trim)
    if (hasError) {
      showErrorDialog("Invalid URL")
    } else {
      try {
        val response = sendRequest(uri, verb)
        val headers = response.headers.map({ case (k, v) => Header(k, v.head) })
        responseCallback(headers, response.body)
      } catch {
        case _: UnknownHostException => showErrorDialog("Unknown Host")
      }
    }
  }

  def sendRequest(uri: String, verb: String): HttpResponse[String] = {
    println("SENDING...")
    Http(uri).option(HttpOptions.followRedirects(true)).method(verb).asString
  }

  def parseURI(rawUri: String): (String, Boolean) = {
    try {
      val rawUriWithProtocol = try {
        new URL(rawUri)
        new URI(rawUri)
        rawUri
      } catch {
        case _: URISyntaxException | _: MalformedURLException => "http://" + rawUri
      }
      val decodedURL = URLDecoder.decode(rawUriWithProtocol, "UTF-8")
      val url = new URL(decodedURL)
      val uri = new URI(url.getProtocol, url.getUserInfo, url.getHost, url.getPort, url.getPath, url.getQuery, url.getRef)
      (uri.toASCIIString, false)
    } catch {
      case _: Exception => (rawUri, true)
    }
  }

  def showErrorDialog(errorMessage: String): Unit = {
    val alert = new Alert(AlertType.ERROR)
    alert.setContentText(errorMessage)
    alert.showAndWait
  }



}

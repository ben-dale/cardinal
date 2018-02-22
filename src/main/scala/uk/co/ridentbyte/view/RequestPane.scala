package uk.co.ridentbyte.view

import java.net._
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.geometry.{Insets, VPos}
import javafx.scene.control.{Button, ChoiceBox, TextField}
import javafx.scene.layout.GridPane

import uk.co.ridentbyte.model.Header
import uk.co.ridentbyte.util.HttpUtil

class RequestPane(responseCallback: (Iterable[Header], String) => Unit, onErrorCallback: (String) => Unit) extends GridPane {

  setGridLinesVisible(true)
  setHgap(10)
  setVgap(10)
  setPadding(new Insets(10, 10, 10, 10))

  val inputUri = new TextField()
  inputUri.setPromptText("http://localhost:8080")
  inputUri.setText("https://google.com")
  add(inputUri, 0, 0)

  val selectVerb = new ChoiceBox[String](FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE", "HEAD", "CONNECT", "OPTIONS", "TRACE", "PATCH"))
  selectVerb.getSelectionModel.selectFirst()
  add(selectVerb, 1, 0)

  val buttonSendRequest = new Button("Send Request")
  buttonSendRequest.setOnAction(sendRequestAction)
  GridPane.setValignment(buttonSendRequest, VPos.TOP)
  add(buttonSendRequest, 1, 1)

  def sendRequestAction(actionEvent: ActionEvent): Unit = {
    val verb = selectVerb.getSelectionModel.getSelectedItem
    val (uri, hasError) = HttpUtil.parseURI(inputUri.getText.trim)
    if (hasError) {
      onErrorCallback("Invalid URL")
    } else {
      try {
        val response = HttpUtil.sendRequest(uri, verb)
        val headers = response.headers.map({ case (k, v) => Header(k, v.head) })
        responseCallback(headers, response.body)
      } catch {
        case _: UnknownHostException => onErrorCallback("Unknown Host")
      }
    }
  }

}

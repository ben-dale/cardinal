package uk.co.ridentbyte.view.request

import java.net._
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.geometry.{HPos, Insets, VPos}
import javafx.scene.control.{Button, ChoiceBox, TextField}
import javafx.scene.layout._
import javafx.scene.paint.Color
import javax.net.ssl.SSLHandshakeException

import uk.co.ridentbyte.model.Header
import uk.co.ridentbyte.util.HttpUtil
import uk.co.ridentbyte.view.util.GridConstraints

class RequestPane(responseCallback: (Int, Iterable[Header], String) => Unit, onErrorCallback: (String) => Unit) extends GridPane {

  setGridLinesVisible(true)
  setHgap(10)
  setVgap(10)
  setPadding(new Insets(10, 10, 10, 10))
  setBackground(new Background(new BackgroundFill(Color.web("#FF0000"), CornerRadii.EMPTY, Insets.EMPTY)))
  getColumnConstraints.add(GridConstraints.widthColumnConstraint(70))
  getColumnConstraints.add(GridConstraints.widthColumnConstraint(30))

  val inputUri = new TextField()
  inputUri.setPromptText("http://localhost:8080")
  inputUri.setText("https://google.com")
  add(inputUri, 0, 0)

  val selectVerb = new ChoiceBox[String](FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE", "HEAD", "CONNECT", "OPTIONS", "TRACE", "PATCH"))
  GridPane.setHalignment(selectVerb, HPos.RIGHT)
  selectVerb.getSelectionModel.selectFirst()
  add(selectVerb, 1, 0)

  val buttonSendRequest = new Button("Send Request")
  buttonSendRequest.setOnAction(sendRequestAction)
  GridPane.setValignment(buttonSendRequest, VPos.TOP)
  GridPane.setHalignment(buttonSendRequest, HPos.RIGHT)
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
        responseCallback(response.code, headers, response.body)
      } catch {
        case _: UnknownHostException => onErrorCallback("Unknown Host")
        case _: SSLHandshakeException => onErrorCallback("SSL Handshake failed. Remote host closed connection during handshake.")
      }
    }
  }

}

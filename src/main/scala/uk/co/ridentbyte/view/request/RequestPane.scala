package uk.co.ridentbyte.view.request

import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.geometry.{HPos, Insets, VPos}
import javafx.scene.control.{Button, ChoiceBox, TextField}
import javafx.scene.layout._
import javafx.scene.paint.Color

class RequestPane(sendRequestCallback: (String, String) => Unit) extends GridPane {

  setGridLinesVisible(true)
  setHgap(10)
  setVgap(10)
  setPadding(new Insets(10, 10, 10, 10))
  setBackground(new Background(new BackgroundFill(Color.web("#FF0000"), CornerRadii.EMPTY, Insets.EMPTY)))

  val inputUri = new TextField()
  GridPane.setHgrow(inputUri, Priority.ALWAYS)
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
    val uri = inputUri.getText.trim
    sendRequestCallback(verb, uri)
  }

}

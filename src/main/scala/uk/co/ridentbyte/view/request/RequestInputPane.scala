package uk.co.ridentbyte.view.request

import javafx.collections.FXCollections
import javafx.geometry.{HPos, Insets}
import javafx.scene.control.{ChoiceBox, TextField}
import javafx.scene.layout._
import javafx.scene.paint.Color

class RequestInputPane extends GridPane {

  setHgap(10)
  setPadding(new Insets(10, 10, 10, 10))
  setBackground(new Background(new BackgroundFill(Color.web("#0000FF"), CornerRadii.EMPTY, Insets.EMPTY)))

  val inputUri = new TextField()
  GridPane.setHgrow(inputUri, Priority.ALWAYS)
  inputUri.setPromptText("http://localhost:8080")
  inputUri.setText("https://google.com")
  add(inputUri, 0, 0)

  val selectVerb = new ChoiceBox[String](FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE", "HEAD", "CONNECT", "OPTIONS", "TRACE", "PATCH"))
  GridPane.setHalignment(selectVerb, HPos.RIGHT)
  selectVerb.getSelectionModel.selectFirst()
  add(selectVerb, 1, 0)

  def getUri: String = inputUri.getText.trim

  def getVerb: String = selectVerb.getSelectionModel.getSelectedItem

}

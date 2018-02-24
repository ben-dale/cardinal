package uk.co.ridentbyte.view.request

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.control._
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout._

class RequestInputPane extends GridPane {

//  setGridLinesVisible(true)
  setHgap(10)
  setVgap(10)
  setPadding(new Insets(10, 10, 10, 10))

  val inputUri = new TextField()
  GridPane.setVgrow(inputUri, Priority.NEVER)
  GridPane.setHgrow(inputUri, Priority.ALWAYS)
  inputUri.setPromptText("http://localhost:8080")
  inputUri.setText("https://google.com")
  add(inputUri, 0, 0)

  val selectVerb = new ChoiceBox[String](FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE", "HEAD", "CONNECT", "OPTIONS", "TRACE", "PATCH"))
  GridPane.setVgrow(selectVerb, Priority.NEVER)
  GridPane.setHgrow(selectVerb, Priority.NEVER)
  selectVerb.getSelectionModel.selectFirst()
  add(selectVerb, 1, 0)

  val labelHeaders = new Label("Headers")
  labelHeaders.setStyle(labelStyle)
  GridPane.setVgrow(labelHeaders, Priority.NEVER)
  GridPane.setHgrow(labelHeaders, Priority.ALWAYS)
  GridPane.setColumnSpan(labelHeaders, 2)
  add(labelHeaders, 0, 1)

  val listHeaders = new ListView[String]()
  listHeaders.setCellFactory(TextFieldListCell.forListView())
  listHeaders.addEventFilter(KeyEvent.KEY_RELEASED, (event: KeyEvent) => {
    val selectedIndex = listHeaders.getSelectionModel.getSelectedIndex
    if (event.getCode == KeyCode.BACK_SPACE && selectedIndex >= 0) {
      listHeaders.getItems.remove(listHeaders.getSelectionModel.getSelectedIndex)
    }
  })
  GridPane.setVgrow(listHeaders, Priority.ALWAYS)
  GridPane.setHgrow(listHeaders, Priority.ALWAYS)
  GridPane.setColumnSpan(listHeaders, 2)
  add(listHeaders, 0, 2)

  val labelBody = new Label("Body")
  labelBody.setStyle(labelStyle)
  GridPane.setVgrow(labelBody, Priority.NEVER)
  GridPane.setHgrow(labelBody, Priority.ALWAYS)
  GridPane.setColumnSpan(labelBody, 2)
  add(labelBody, 0, 3)

  val textAreaBody = new TextArea()
  GridPane.setColumnSpan(textAreaBody, 2)
  GridPane.setHgrow(textAreaBody, Priority.ALWAYS)
  GridPane.setVgrow(textAreaBody, Priority.ALWAYS)
  add(textAreaBody, 0, 4)


  def getUri: String = inputUri.getText.trim

  def getVerb: String = selectVerb.getSelectionModel.getSelectedItem

  def addHeader(header: String): Unit = listHeaders.getItems.add(header)

  private def labelStyle: String = {
    """
      |-fx-font-size: 12;
    """.stripMargin
  }

}

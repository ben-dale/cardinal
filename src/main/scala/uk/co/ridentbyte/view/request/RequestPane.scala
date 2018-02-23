package uk.co.ridentbyte.view.request

import javafx.beans.property.SimpleStringProperty
import javafx.collections.{FXCollections, ObservableList}
import javafx.geometry.{Insets, VPos}
import javafx.scene.control.cell.{PropertyValueFactory, TextFieldListCell, TextFieldTableCell}
import javafx.scene.control._
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout._
import javafx.scene.paint.Color

class RequestPane(sendRequestCallback: (String, String) => Unit) extends GridPane {

  setBackground(new Background(new BackgroundFill(Color.web("#FF0000"), CornerRadii.EMPTY, Insets.EMPTY)))

  setStyle(
    """
      |-fx-border-width: 0 1 0 0;
      |-fx-border-color: grey;
      |-fx-border-style: hidden solid hidden hidden;
    """.stripMargin)

  val requestInputPane = new RequestInputPane
  GridPane.setHgrow(requestInputPane, Priority.ALWAYS)
  add(requestInputPane, 0, 0)

  val headers = new ListView[String]()
  headers.setCellFactory(TextFieldListCell.forListView())
  headers.addEventFilter(KeyEvent.KEY_RELEASED, (event: KeyEvent) => {
    val selectedIndex = headers.getSelectionModel.getSelectedIndex
    if (event.getCode == KeyCode.BACK_SPACE && selectedIndex >= 0) {
      headers.getItems.remove(headers.getSelectionModel.getSelectedIndex)
    }
  })
  GridPane.setVgrow(headers, Priority.ALWAYS)
  add(headers, 0, 1)

  val body = new TextArea()
  GridPane.setVgrow(body, Priority.ALWAYS)
  add(body, 0, 2)

  val requestControlPane = new RequestControlPane(sendRequest, showAddHeader)
  GridPane.setHgrow(requestControlPane, Priority.ALWAYS)
  GridPane.setValignment(requestControlPane, VPos.BASELINE)
  add(requestControlPane, 0, 3)

  def sendRequest(): Unit = {
    val verb = requestInputPane.getVerb
    val uri = requestInputPane.getUri
    sendRequestCallback(verb, uri)
  }

  def showAddHeader(): Unit = {
    val dialog = new TextInputDialog
    dialog.setTitle("New header")
    dialog.setHeaderText("New header")
    dialog.setContentText("Header")
    val result = dialog.showAndWait()
    result.ifPresent((v) => if (v.trim.length > 0) headers.getItems.add(v.trim))
  }

}

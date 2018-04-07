package uk.co.ridentbyte.view.request

import javafx.event.ActionEvent
import javafx.geometry.HPos
import javafx.scene.control.{Button, Label, ListView}
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.layout.{GridPane, Priority}

import scala.collection.JavaConverters._

class RequestHeadersInputPane(triggerUnsavedChangesMade: () => Unit) extends GridPane {

  setHgap(5)
  setVgap(5)

  private val labelHeaders = new Label("Headers")
  labelHeaders.getStyleClass.add("cardinal-font")
  GridPane.setColumnSpan(labelHeaders, 2)
  GridPane.setVgrow(labelHeaders, Priority.NEVER)
  GridPane.setHgrow(labelHeaders, Priority.ALWAYS)
  add(labelHeaders, 0, 0)

  private val listHeaders = new ListView[String]()
  listHeaders.getItems.addListener((_) => triggerUnsavedChangesMade())
  listHeaders.setEditable(true)
  listHeaders.getStyleClass.add("cardinal-font")
  listHeaders.setCellFactory(TextFieldListCell.forListView())
  GridPane.setColumnSpan(listHeaders, 2)
  GridPane.setVgrow(listHeaders, Priority.ALWAYS)
  GridPane.setHgrow(listHeaders, Priority.ALWAYS)
  add(listHeaders, 0, 1)

  private val buttonRemoveHeader = new Button("-")
  buttonRemoveHeader.setOnAction(removeHeaderAction)
  buttonRemoveHeader.setStyle("-fx-padding: 2 6 2 6;")
  buttonRemoveHeader.getStyleClass.add("cardinal-font")
  GridPane.setColumnSpan(buttonRemoveHeader, 1)
  GridPane.setHalignment(buttonRemoveHeader, HPos.RIGHT)
  GridPane.setVgrow(buttonRemoveHeader, Priority.NEVER)
  GridPane.setHgrow(buttonRemoveHeader, Priority.ALWAYS)
  add(buttonRemoveHeader, 0, 2)

  private val buttonAddHeader = new Button("+")
  buttonAddHeader.setOnAction(addNewHeaderAction)
  buttonAddHeader.setStyle("-fx-padding: 2 6 2 6;")
  buttonAddHeader.getStyleClass.add("cardinal-font")
  GridPane.setColumnSpan(buttonAddHeader, 1)
  GridPane.setVgrow(buttonAddHeader, Priority.NEVER)
  GridPane.setHgrow(buttonAddHeader, Priority.NEVER)
  add(buttonAddHeader, 1, 2)

  def getHeaders: List[String] = {
    listHeaders.getItems.asScala.toList
  }

  def clear(): Unit = {
    listHeaders.getItems.clear()
  }

  def addHeader(header: String): Unit = {
    listHeaders.getItems.add(header)
  }

  private def removeHeaderAction(actionEvent: ActionEvent): Unit = {
    val selectedIndex = listHeaders.getSelectionModel.getSelectedIndex
    if (selectedIndex >= 0) {
      listHeaders.getItems.remove(selectedIndex)
    }
  }

  private def addNewHeaderAction(actionEvent: ActionEvent): Unit = {
    listHeaders.getItems.add("")
    val index = listHeaders.getItems.size - 1
    listHeaders.requestFocus()
    listHeaders.getSelectionModel.select(index)
  }
}

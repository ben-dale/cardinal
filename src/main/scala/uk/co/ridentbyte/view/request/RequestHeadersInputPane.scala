package uk.co.ridentbyte.view.request

import javafx.scene.control.{Label, ListView}
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.{GridPane, Priority, RowConstraints}

import scala.collection.JavaConverters._

class RequestHeadersInputPane extends GridPane {

  setHgap(10)
  setVgap(10)

  val labelHeaders = new Label("Headers")
  labelHeaders.setStyle(labelStyle)
  GridPane.setVgrow(labelHeaders, Priority.NEVER)
  GridPane.setHgrow(labelHeaders, Priority.ALWAYS)
  add(labelHeaders, 0, 0)

  val listHeaders = new ListView[String]()
  listHeaders.setStyle(
    """
      |-fx-font-family: Monospaced;
      |-fx-font-size: 13;
      |-fx-font-weight: 600;
    """.stripMargin
  )
  listHeaders.setCellFactory(TextFieldListCell.forListView())
  listHeaders.addEventFilter(KeyEvent.KEY_RELEASED, (event: KeyEvent) => {
    val selectedIndex = listHeaders.getSelectionModel.getSelectedIndex
    if (event.getCode == KeyCode.BACK_SPACE && selectedIndex >= 0) {
      listHeaders.getItems.remove(listHeaders.getSelectionModel.getSelectedIndex)
    }
  })
  GridPane.setVgrow(listHeaders, Priority.ALWAYS)
  GridPane.setHgrow(listHeaders, Priority.ALWAYS)
  add(listHeaders, 0, 1)

  private def labelStyle: String = {
    """
      |-fx-font-size: 12;
    """.stripMargin
  }

  def getHeaders: List[String] = {
    listHeaders.getItems.asScala.toList
  }

  def clear(): Unit = {
    listHeaders.getItems.clear()
  }

  def addHeader(header: String): Unit = {
    listHeaders.getItems.add(header)
  }

}

package uk.co.ridentbyte.view.request

import javafx.application.Platform
import javafx.geometry.HPos
import javafx.scene.control.Button
import javafx.scene.layout._

class RequestControlPane(exportToCurl: () => Unit,
                         sendRequest: (() => Unit, () => Unit) => Unit,
                         showBulkRequest: () => Unit) extends GridPane {

  setHgap(10)
  setVgap(10)
  getStyleClass.add("control-pane")

  private val buttonExportToCurl = new Button("As cURL")
  buttonExportToCurl.getStyleClass.add("cardinal-font")
  GridPane.setVgrow(buttonExportToCurl, Priority.NEVER)
  GridPane.setHgrow(buttonExportToCurl, Priority.ALWAYS)
  GridPane.setHalignment(buttonExportToCurl, HPos.RIGHT)
  buttonExportToCurl.setOnAction(_ => exportToCurl())
  add(buttonExportToCurl, 0, 0)

  private val buttonSendBulkRequest = new Button("Send Bulk Request...")
  buttonSendBulkRequest.getStyleClass.add("cardinal-font")
  GridPane.setVgrow(buttonSendBulkRequest, Priority.NEVER)
  GridPane.setHgrow(buttonSendBulkRequest, Priority.NEVER)
  buttonSendBulkRequest.setOnAction(_ => showBulkRequest())
  add(buttonSendBulkRequest, 1, 0)

  private val buttonSendRequest = new Button("Send Request")
  buttonSendRequest.setMinWidth(120)
  buttonSendRequest.setMaxWidth(120)
  buttonSendRequest.setPrefWidth(120)
  buttonSendRequest.getStyleClass.add("cardinal-font")
  GridPane.setVgrow(buttonSendRequest, Priority.NEVER)
  GridPane.setHgrow(buttonSendRequest, Priority.NEVER)
  buttonSendRequest.setOnAction(_ => {
    val onStart = () => {
      Platform.runLater(() => {
        buttonSendRequest.setText("Sending...")
        buttonSendRequest.setDisable(true)
      })
    }

    val onFinish = () => {
      Platform.runLater(() => {
        buttonSendRequest.setText("Send Request")
        buttonSendRequest.setDisable(false)
      })
    }

    sendRequest(onStart, onFinish)
  })
  add(buttonSendRequest, 2, 0)

  def lock(): Unit = {
    buttonExportToCurl.setDisabled(true)
    buttonSendBulkRequest.setDisabled(true)
    buttonSendRequest.setDisabled(true)
  }

  def unlock(): Unit = {
    buttonExportToCurl.setDisabled(false)
    buttonSendBulkRequest.setDisabled(false)
    buttonSendRequest.setDisabled(false)
  }

}

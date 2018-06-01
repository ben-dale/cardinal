package uk.co.ridentbyte.view.request

import javafx.application.Platform
import javafx.geometry.HPos
import javafx.scene.control.Button
import javafx.scene.layout._

class RequestControlPane(sendRequestCallback: (() => Unit, () => Unit) => Unit, showBulkRequestCallback: () => Unit) extends GridPane {

  setHgap(10)
  setVgap(10)
  getStyleClass.add("control-pane")

  private val buttonSendBulkRequest = new Button("Send Bulk Request...")
  buttonSendBulkRequest.getStyleClass.add("cardinal-font")
  GridPane.setVgrow(buttonSendBulkRequest, Priority.NEVER)
  GridPane.setHgrow(buttonSendBulkRequest, Priority.ALWAYS)
  GridPane.setHalignment(buttonSendBulkRequest, HPos.RIGHT)
  buttonSendBulkRequest.setOnAction(_ => showBulkRequestCallback())
  add(buttonSendBulkRequest, 0, 0)

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

    sendRequestCallback(onStart, onFinish)
  })
  add(buttonSendRequest, 1, 0)

}

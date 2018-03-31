package uk.co.ridentbyte.view.request

import javafx.geometry.HPos
import javafx.scene.control.Button
import javafx.scene.layout._

class RequestControlPane(sendRequestCallback: () => Unit, showBulkRequestCallback: () => Unit) extends GridPane {

  setHgap(10)
  setVgap(10)
  getStyleClass.add("control-pane")

  private val buttonSendBulkRequest = new Button("Send Bulk Request...")
  GridPane.setVgrow(buttonSendBulkRequest, Priority.NEVER)
  GridPane.setHgrow(buttonSendBulkRequest, Priority.ALWAYS)
  GridPane.setHalignment(buttonSendBulkRequest, HPos.RIGHT)
  buttonSendBulkRequest.setOnAction((_) => showBulkRequestCallback())
  add(buttonSendBulkRequest, 0, 0)

  private val buttonSendRequest = new Button("Send Request")
  GridPane.setVgrow(buttonSendRequest, Priority.NEVER)
  GridPane.setHgrow(buttonSendRequest, Priority.NEVER)
  buttonSendRequest.setOnAction((_) => sendRequestCallback())
  add(buttonSendRequest, 1, 0)

}

package uk.co.ridentbyte.view.request

import javafx.geometry.HPos
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.{ButtonType, Dialog, Label, TextField}
import javafx.scene.layout.GridPane
import javafx.application.Platform

class BulkRequestInputDialog extends Dialog[(String, String, String)] {

  setTitle("Bulk Request")

  val buttonSendBulkRequest = new ButtonType("Send Bulk Request", ButtonData.OK_DONE)
  getDialogPane.getButtonTypes.addAll(ButtonType.CANCEL, buttonSendBulkRequest)

  val grid = new GridPane
  grid.setHgap(10)
  grid.setVgap(10)

  val labelDelay = new Label("Delay per request (ms)")
  GridPane.setHalignment(labelDelay, HPos.RIGHT)
  grid.add(labelDelay, 0, 0)

  val textDelay = new TextField
  grid.add(textDelay, 1, 0)

  val labelNumOfRequests = new Label("No. of requests")
  GridPane.setHalignment(labelNumOfRequests, HPos.RIGHT)
  grid.add(labelNumOfRequests, 0, 1)

  val textNumOfRequests = new TextField
  grid.add(textNumOfRequests, 1, 1)

  val labelForEachIn = new Label("For each in")
  GridPane.setHalignment(labelForEachIn, HPos.RIGHT)
  grid.add(labelForEachIn, 0, 2)

  val textForEachIn = new TextField
  grid.add(textForEachIn, 1, 2)

  getDialogPane.setContent(grid)

  Platform.runLater(() => textDelay.requestFocus())

  setResultConverter((buttonType) => {
    if (buttonType == buttonSendBulkRequest) {
      (textDelay.getText, textNumOfRequests.getText, textForEachIn.getText)
    } else {
      null
    }
  })
}

package uk.co.ridentbyte.view.request

import javafx.geometry.HPos
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control._
import javafx.scene.layout.GridPane
import javafx.application.Platform

import uk.co.ridentbyte.model.BulkRequest

class BulkRequestInputDialog(bulkRequest: BulkRequest) extends Dialog[BulkRequest] {

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
  textDelay.setText(bulkRequest.getThrottleAsString)
  textDelay.setText("500")
  grid.add(textDelay, 1, 0)

  val separator = new Separator()
  GridPane.setColumnSpan(separator, 2)
  grid.add(separator, 0, 1)

  val labelNumOfRequests = new Label("No. of requests")
  GridPane.setHalignment(labelNumOfRequests, HPos.RIGHT)
  grid.add(labelNumOfRequests, 0, 2)

  val textNumOfRequests = new TextField
  textNumOfRequests.setText(bulkRequest.getCountAsString)
  grid.add(textNumOfRequests, 1, 2)

  val labelOr = new Label("- OR -")
  GridPane.setColumnSpan(labelOr, 2)
  GridPane.setHalignment(labelOr, HPos.CENTER)
  grid.add(labelOr, 0, 3)

  val labelForEach = new Label("For each")
  GridPane.setHalignment(labelForEach, HPos.RIGHT)
  grid.add(labelForEach, 0, 4)

  val textForEach = new TextField
  textForEach.setText(bulkRequest.getIdsAsString)
  textForEach.setPromptText("325, 454, 432...")
  grid.add(textForEach, 1, 4)

  getDialogPane.setContent(grid)

  Platform.runLater(() => textDelay.requestFocus())

  setResultConverter((buttonType) => {
    if (buttonType == buttonSendBulkRequest) {
      BulkRequest(
        if (textDelay.getText.trim.length == 0) {
          None
        } else {
          try {
            Some(textDelay.getText.trim.toLong)
          } catch {
            case _: Exception => None
          }
        },
        if (textNumOfRequests.getText.trim.length == 0) {
          None
        } else {
          try {
            Some(textNumOfRequests.getText.trim.toInt)
          } catch {
            case _: Exception => None
          }
        },
        if (textForEach.getText.trim.length == 0) None else Some(textForEach.getText.trim.split(",").toList)
      )
    } else {
      null
    }
  })
}

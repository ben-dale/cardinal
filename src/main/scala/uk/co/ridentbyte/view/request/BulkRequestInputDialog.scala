package uk.co.ridentbyte.view.request

import javafx.geometry.HPos
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control._
import javafx.scene.layout.GridPane
import javafx.application.Platform

class BulkRequestInputDialog(data: (Option[Long], Option[Int], Option[String]) = (None, None, None)) extends Dialog[(String, String, String)] {

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
  textDelay.setText(if (data._1.isDefined) data._1.get.toString else "")
  textDelay.setText("500")
  grid.add(textDelay, 1, 0)

  val separator = new Separator()
  GridPane.setColumnSpan(separator, 2)
  grid.add(separator, 0, 1)

  val labelNumOfRequests = new Label("No. of requests")
  GridPane.setHalignment(labelNumOfRequests, HPos.RIGHT)
  grid.add(labelNumOfRequests, 0, 2)

  val textNumOfRequests = new TextField
  textNumOfRequests.setText(if (data._2.isDefined) data._2.get.toString else "")
  grid.add(textNumOfRequests, 1, 2)

  val labelOr = new Label("- OR -")
  GridPane.setColumnSpan(labelOr, 2)
  GridPane.setHalignment(labelOr, HPos.CENTER)
  grid.add(labelOr, 0, 3)

  val labelForEachIn = new Label("For each in")
  GridPane.setHalignment(labelForEachIn, HPos.RIGHT)
  grid.add(labelForEachIn, 0, 4)

  val textForEachIn = new TextField
  textForEachIn.setText(data._3.getOrElse(""))
  textForEachIn.setPromptText("325, 454, 432...")
  grid.add(textForEachIn, 1, 4)

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

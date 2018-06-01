package uk.co.ridentbyte.view.dialog

import javafx.geometry.{HPos, Insets}
import javafx.scene.control._
import javafx.scene.layout.{GridPane, Priority}
import uk.co.ridentbyte.model.BulkRequest

class BulkRequestInputDialog(bulkRequest: Option[BulkRequest] = None) extends Dialog[BulkRequest] {

  setTitle("Bulk Request")
  getDialogPane.getButtonTypes.addAll(ButtonType.CANCEL, ButtonType.OK)

  private val grid = new GridPane
  grid.setPadding(new Insets(10, 10, 10, 10))
  grid.setHgap(10)
  grid.setVgap(10)

  private val labelDelay = new Label("Delay per request (ms)")
  GridPane.setHalignment(labelDelay, HPos.RIGHT)
  grid.add(labelDelay, 0, 0)

  private val throttle = if (bulkRequest.isEmpty) 200 else bulkRequest.get.throttle.get
  private val textDelay = new TextField(throttle.toString)
  GridPane.setHgrow(textDelay, Priority.ALWAYS)
  textDelay.setText("200")
  grid.add(textDelay, 1, 0)

  private val labelNumOfRequests = new Label("No. of requests")
  GridPane.setHalignment(labelNumOfRequests, HPos.RIGHT)
  grid.add(labelNumOfRequests, 0, 1)

  private val numOfRequests = bulkRequest match {
    case Some(r) if r.count.isDefined => r.count.get.toString
    case _ => ""
  }
  private val textNumOfRequests = new TextField(numOfRequests)
  GridPane.setHgrow(textNumOfRequests, Priority.ALWAYS)
  grid.add(textNumOfRequests, 1, 1)

  private val labelOr = new Label("- OR -")
  GridPane.setColumnSpan(labelOr, 3)
  GridPane.setHalignment(labelOr, HPos.CENTER)
  grid.add(labelOr, 0, 2)

  private val labelForEach = new Label("For each")
  GridPane.setHalignment(labelForEach, HPos.RIGHT)
  grid.add(labelForEach, 0, 3)

  private val ids = bulkRequest match {
    case Some(r) if r.ids.isDefined => r.ids.get.mkString(",")
    case _ => ""
  }
  private val textForEach = new TextField(ids)
  GridPane.setHgrow(textForEach, Priority.ALWAYS)
  textForEach.setPromptText("325, 454, 432 or 12..54")
  grid.add(textForEach, 1, 3)

  getDialogPane.setContent(grid)

  setResultConverter((buttonType) => {
    if (buttonType == ButtonType.OK) {
      BulkRequest(getOptTextDelay, getOptNumberOfRequests, getTextForEach)
    } else {
      null
    }
  })

  def getOptTextDelay: Option[Long] = {
    if (textDelay.getText.trim.length == 0) {
      None
    } else {
      try {
        Some(textDelay.getText.trim.toLong)
      } catch {
        case _: Exception => None
      }
    }
  }

  def getOptNumberOfRequests: Option[Int] = {
    if (textNumOfRequests.getText.trim.length == 0) {
      None
    } else {
      try {
        Some(textNumOfRequests.getText.trim.toInt)
      } catch {
        case _: Exception => None
      }
    }
  }

  def getTextForEach: Option[List[String]] = {
    if (textForEach.getText.trim.length == 0) {
      None
    } else {
      val matcher = "([0-9]+)..([0-9]+)".r
      textForEach.getText.trim match {
        case matcher(a, b) => Some(a.toInt.to(b.toInt).map(_.toString).toList)
        case v => Some(v.split(",").toList)
      }
    }
  }
}

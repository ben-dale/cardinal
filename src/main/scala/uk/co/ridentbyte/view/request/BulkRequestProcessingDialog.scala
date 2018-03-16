package uk.co.ridentbyte.view.request

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.geometry.HPos
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control._
import javafx.scene.layout.{Background, BackgroundFill, GridPane, Priority}
import javafx.scene.paint.Color

import uk.co.ridentbyte.model.{HttpResponseWrapper, Request}

class BulkRequestProcessingDialog(requestCount: Option[Int],
                                  throttle: Option[Long],
                                  ids: Option[List[String]],
                                  request: Request,
                                  sendRequestCallback: (Request) => HttpResponseWrapper,
                                  finishedBulkRequestCallback: (List[HttpResponseWrapper]) => Unit) extends Dialog[Void] {

  private var allResponses = collection.mutable.ListBuffer.empty[HttpResponseWrapper]

  private val task = new Task[Boolean]() {
    override def call(): Boolean = {
      if (throttle.isDefined && requestCount.isDefined) {
        1 to requestCount.get foreach { i =>
          Thread.sleep(throttle.get)
          val r = request.withId(i.toString).processConstants()
          val response = sendRequestCallback(r)
          allResponses += response
          textAreaConsole.appendText("[HTTP " + response.httpResponse.code + "] - " + r.verb + " " + r.uri + "\n")
          updateProgress(i, requestCount.get)
        }
        finishedBulkRequestCallback(allResponses.toList)
        Platform.runLater(() => close())
        true
      } else if (throttle.isDefined && ids.isDefined) {
        ids.get.zipWithIndex.foreach { case (id, i) =>
          Thread.sleep(throttle.get)
          val r = request.withId(id).processConstants()
          val response = sendRequestCallback(r)
          allResponses += response
          textAreaConsole.appendText("[HTTP " + response.httpResponse.code + "] - " + r.verb + " " + r.uri + "\n")
          updateProgress(i + 1, ids.get.length.toDouble)
        }
        finishedBulkRequestCallback(allResponses.toList)
        Platform.runLater(() => close())
        true
      } else {
        false
      }
    }
  }

  setTitle("Bulk Request")

  val grid = new GridPane
  grid.setHgap(10)
  grid.setVgap(10)
  grid.setMinWidth(500)

  val labelSendingRequest = new Label("Sending requests...")
  GridPane.setHalignment(labelSendingRequest, HPos.CENTER)
  GridPane.setFillWidth(labelSendingRequest, true)
  grid.add(labelSendingRequest, 0, 0)

  val progressBar = new ProgressBar()
  progressBar.progressProperty().unbind()
  progressBar.progressProperty().bind(task.progressProperty())
  progressBar.setMaxWidth(java.lang.Double.MAX_VALUE)
  GridPane.setFillWidth(progressBar, true)
  grid.add(progressBar, 0, 1)

  val textAreaConsole = new TextArea
  textAreaConsole.setStyle(
    """
      |-fx-font-family: Monospaced;
      |-fx-font-size: 12;
    """.stripMargin
  )
  GridPane.setHgrow(textAreaConsole, Priority.ALWAYS)
  grid.add(textAreaConsole, 0, 2)

  getDialogPane.setContent(grid)
  getDialogPane.getButtonTypes.addAll(new ButtonType("Abort", ButtonData.CANCEL_CLOSE))

  setOnCloseRequest((_) => {
    task.cancel()
  })

  // Start the process
  new Thread(task).start()
}

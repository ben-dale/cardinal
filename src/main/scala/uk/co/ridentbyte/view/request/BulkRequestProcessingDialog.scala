package uk.co.ridentbyte.view.request

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.geometry.HPos
import javafx.scene.control.{ButtonType, Dialog, Label, ProgressBar}
import javafx.scene.layout.{GridPane, Priority}

import uk.co.ridentbyte.model.{HttpResponseWrapper, Request}

class BulkRequestProcessingDialog(requestCount: Int,
                                  throttle: Long,
                                  request: Request,
                                  sendRequestCallback: (Request) => HttpResponseWrapper,
                                  finishedBulkRequestCallback: (List[Option[HttpResponseWrapper]]) => Unit) extends Dialog[Void] {

  private var allResponses = collection.mutable.ListBuffer.empty[Option[HttpResponseWrapper]]

  private val task = new Task[Boolean]() {
    override def call(): Boolean = {
      1 to requestCount foreach { i =>
        Thread.sleep(throttle)
        try {
          val response = sendRequestCallback(request)
          allResponses += Some(response)
        } catch {
          case _: Exception => allResponses += None
        }
        updateProgress(i, requestCount)
      }
      finishedBulkRequestCallback(allResponses.toList)
      Platform.runLater(() => close())
      true
    }
  }

  setTitle("Bulk Request")

  val grid = new GridPane
  grid.setHgap(10)
  grid.setVgap(10)
  grid.setMinWidth(400)

  val labelSendingRequest = new Label("Sending requests...")
  GridPane.setHgrow(labelSendingRequest, Priority.ALWAYS)
  GridPane.setHalignment(labelSendingRequest, HPos.CENTER)
  GridPane.setFillWidth(labelSendingRequest, true)
  grid.add(labelSendingRequest, 0, 0)

  val progressBar = new ProgressBar()
  progressBar.progressProperty().unbind()
  progressBar.progressProperty().bind(task.progressProperty())
  GridPane.setHgrow(progressBar, Priority.ALWAYS)
  GridPane.setFillWidth(progressBar, true)
  progressBar.setPrefWidth(400)
  grid.add(progressBar, 0, 1)

  getDialogPane.setContent(grid)

  getDialogPane.getButtonTypes.addAll(ButtonType.CANCEL)

  new Thread(task).start()
}

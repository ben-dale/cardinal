package uk.co.ridentbyte.view.response

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.geometry.{HPos, Insets}
import javafx.scene.control.{Button, Label, ProgressBar}
import javafx.scene.layout.{GridPane, Priority}
import uk.co.ridentbyte.model._
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}
import scala.collection.JavaConverters._

case class BulkRequestProcessingOutputPane(getConfigCallback: java.util.function.Function[Void, Config],
                                           sendRequestCallback: java.util.function.Function[CardinalRequest, CardinalResponse],
                                           finishedBulkRequestCallback: java.util.function.BiFunction[java.util.List[CardinalRequestAndResponse], Integer, Void],
                                           bulkRequest: CardinalBulkRequest) extends GridPane {

  var requestsAndResponses = collection.mutable.ListBuffer.empty[CardinalRequestAndResponse]
  val labelDelta = new Label()
  val throttle = bulkRequest.getThrottle
  val requestCount = bulkRequest.getRequestCount
  val request = bulkRequest.getRequest
  val ids = bulkRequest.getIds

  val task = new Task[Boolean]() {
    override def call(): Boolean = {
      if (requestCount > 0) {
        0 until requestCount foreach { i =>
          Thread.sleep(throttle)
          val r = request.withId(i.toString).processConstants(getConfigCallback.apply(null))
          try {
            val response = sendRequestCallback(r)
            requestsAndResponses += new CardinalRequestAndResponse(r, response)
            updateProgress(i + 1, requestCount)
            Platform.runLater(() => {
              labelDelta.setText((i + 1).toString)
            })
          } catch {
            case _: Exception =>
              // Todo - update exception log to show on screen?
              requestsAndResponses += new CardinalRequestAndResponse(r, null)
              updateProgress(i + 1, requestCount)
              Platform.runLater(() => {
                labelDelta.setText((i + 1).toString)
              })
          }
        }
        finishedBulkRequestCallback(requestsAndResponses.toList.asJava, throttle)
        true
      } else {
        ids.asScala.zipWithIndex.foreach { case (id, i) =>
          Thread.sleep(throttle)
          val r = request.withId(id).processConstants(getConfigCallback.apply(null))
          try {
            val response = sendRequestCallback(r)
            requestsAndResponses += new CardinalRequestAndResponse(r, response)
            updateProgress(i + 1, ids.size)
            Platform.runLater(() => {
              labelDelta.setText((i + 1).toString)
            })
          } catch {
            case _: Exception =>
              // Todo - update exception log to show on screen?
              requestsAndResponses += new CardinalRequestAndResponse(r, null)
              updateProgress(i + 1, ids.size())
              Platform.runLater(() => {
                labelDelta.setText((i + 1).toString)
              })
          }
        }
        finishedBulkRequestCallback(requestsAndResponses.toList.asJava, throttle)
        true
      }
    }
  }

  setHgap(10)
  setVgap(10)
  setPadding(new Insets(20, 60, 20, 60))
  getStyleClass.addAll("plain-border", "round-border")
  getRowConstraints.addAll(
    new RowConstraintsBuilder().withVgrow(Priority.NEVER).build,
    new RowConstraintsBuilder().withVgrow(Priority.NEVER).build,
    new RowConstraintsBuilder().withVgrow(Priority.NEVER).build,
    new RowConstraintsBuilder().withVgrow(Priority.NEVER).build
  )
  getColumnConstraints.addAll(
    new ColumnConstraintsBuilder().withPercentageWidth(100).build
  )

  private val labelHeader = new Label("Processing Requests")
  labelHeader.getStyleClass.addAll("header")
  GridPane.setHalignment(labelHeader, HPos.CENTER)
  add(labelHeader, 0, 0)

  val progressBar = new ProgressBar()
  progressBar.progressProperty().unbind()
  progressBar.progressProperty().bind(task.progressProperty())
  progressBar.setMaxWidth(java.lang.Double.MAX_VALUE)
  GridPane.setHgrow(progressBar, Priority.ALWAYS)
  add(progressBar, 0, 1)

  GridPane.setHalignment(labelDelta, HPos.CENTER)
  GridPane.setHgrow(labelDelta, Priority.ALWAYS)
  GridPane.setFillWidth(labelDelta, true)
  add(labelDelta, 0, 2)

  val buttonAbort = new Button("Abort")
  buttonAbort.setOnAction(_ => {
    task.cancel()
    finishedBulkRequestCallback(requestsAndResponses.toList.asJava, throttle)
  })
  GridPane.setHalignment(buttonAbort, HPos.CENTER)
  add(buttonAbort, 0, 3)


}

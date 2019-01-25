package uk.co.ridentbyte.view.response

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.geometry.{HPos, Insets}
import javafx.scene.control.{Button, Label, ProgressBar}
import javafx.scene.layout.{GridPane, Priority}
import uk.co.ridentbyte.model.{CardinalRequest, CardinalResponse, Config}
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}

case class BulkRequestProcessingOutputPane(getConfigCallback: () => Config,
                                      sendRequestCallback: CardinalRequest => CardinalResponse,
                                      finishedBulkRequestCallback: (List[(CardinalRequest, Option[CardinalResponse])], Option[Long]) => Unit,
                                      request: CardinalRequest,
                                      throttle: Option[Long],
                                      requestCount: Option[Int],
                                      ids: Option[List[String]]) extends GridPane {

  var requestsAndResponses = collection.mutable.ListBuffer.empty[(CardinalRequest, Option[CardinalResponse])]
  val labelDelta = new Label()

  val task = new Task[Boolean]() {
    override def call(): Boolean = {
      if (throttle.isDefined && requestCount.isDefined) {
        0 until requestCount.get foreach { i =>
          Thread.sleep(throttle.get)
          val r = request.withId(i.toString).processConstants(getConfigCallback())
          try {
            val response = sendRequestCallback(r)
            requestsAndResponses += ((r, Some(response)))
            updateProgress(i + 1, requestCount.get)
            Platform.runLater(() => {
              labelDelta.setText((i + 1).toString)
            })
          } catch {
            case _: Exception =>
              // Todo - update exception log to show on screen?
              requestsAndResponses += ((r, None))
              updateProgress(i + 1, requestCount.get)
              Platform.runLater(() => {
                labelDelta.setText((i + 1).toString)
              })
          }
        }
        finishedBulkRequestCallback(requestsAndResponses.toList, throttle)
        true
      } else if (throttle.isDefined && ids.isDefined) {
        ids.get.zipWithIndex.foreach { case (id, i) =>
          Thread.sleep(throttle.get)
          val r = request.withId(id).processConstants(getConfigCallback())
          try {
            val response = sendRequestCallback(r)
            requestsAndResponses += ((r, Some(response)))
            updateProgress(i + 1, ids.get.length.toDouble)
            Platform.runLater(() => {
              labelDelta.setText((i + 1).toString)
            })
          } catch {
            case _: Exception =>
              // Todo - update exception log to show on screen?
              requestsAndResponses += ((r, None))
              updateProgress(i + 1, ids.get.length.toDouble)
              Platform.runLater(() => {
                labelDelta.setText((i + 1).toString)
              })
          }
        }
        finishedBulkRequestCallback(requestsAndResponses.toList, throttle)
        true
      } else {
        false
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
    finishedBulkRequestCallback(requestsAndResponses.toList, throttle)
  })
  GridPane.setHalignment(buttonAbort, HPos.CENTER)
  add(buttonAbort, 0, 3)


}

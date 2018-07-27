package uk.co.ridentbyte.view.response

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.geometry.{HPos, Insets}
import javafx.scene.control._
import javafx.scene.layout._
import javafx.scene.paint.Color
import uk.co.ridentbyte.model.{BulkRequest, Config, CardinalResponse, CardinalRequest}
import uk.co.ridentbyte.view.dialog.BulkRequestInputDialog
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}

class ResponsePane(getConfigCallback: () => Config,
                   sendRequestCallback: CardinalRequest => CardinalResponse,
                   exportToCsv: List[(CardinalRequest, Option[CardinalResponse])] => Unit,
                   exportToBash: (List[CardinalRequest], Option[Long]) => Unit,
                   showErrorDialogCallback: String => Unit) extends BorderPane {

  setPadding(new Insets(20, 20, 20, 20))
  clearContents()

  def clearContents(): Unit = {
    val emptyPanel = new BorderPane
    emptyPanel.getStyleClass.addAll("dashed-border", "round-border")
    emptyPanel.setBorder(new Border(new BorderStroke(Color.LIGHTGREY, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)))
    setCenter(emptyPanel)
  }

  def setResponse(response: Option[CardinalResponse]): Unit = {
    if (response.isDefined) {
      val grid = new GridPane
      grid.setVgap(10)
      grid.setHgap(10)
      grid.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build)
      grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).withPercentageHeight(40).build)
      grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).withPercentageHeight(60).build)

      val listHeaders = new ListView[String]()
      listHeaders.getStyleClass.add("cardinal-font")
      response.get.raw.headers.foreach {
        header => listHeaders.getItems.add(header._1 + ": " + header._2.mkString(""))
      }
      grid.add(listHeaders, 0, 0)

      val textAreaBody = new TextArea()
      textAreaBody.setEditable(false)
      textAreaBody.getStyleClass.add("cardinal-font")
      textAreaBody.setText(response.get.formattedBody)
      grid.add(textAreaBody, 0, 1)

      Platform.runLater(() => setCenter(grid))
    }
  }

  def loadCurlCommand(command: String): Unit = {
    Platform.runLater(() => setCenter(CurlOutput(command)))
  }

  def showBulkRequestInput(request: CardinalRequest, bulkRequest: Option[BulkRequest] = None): Unit = {
    val bulkRequestInputDialog = new BulkRequestInputDialog(bulkRequest)
    val bulkRequestResult = bulkRequestInputDialog.showAndWait()
    if (bulkRequestResult != null && bulkRequestResult.isPresent) {
      if (bulkRequestResult.get.throttle.isEmpty || (bulkRequestResult.get.count.isEmpty && bulkRequestResult.get.ids.isEmpty)) {
        showErrorDialogCallback("Invalid input. \nPlease provide a throttle and either a request count or a range value.")
        showBulkRequestInput(request, Some(bulkRequestResult.get))
      } else if (bulkRequestResult.get.asBash) {
        asScript(request, bulkRequestResult.get.throttle, bulkRequestResult.get.count, bulkRequestResult.get.ids)
      } else {
        startBulkRequest(request, bulkRequestResult.get.throttle, bulkRequestResult.get.count, bulkRequestResult.get.ids)
      }
    }
  }

  def asScript(request: CardinalRequest, throttle: Option[Long], requestCount: Option[Int], ids: Option[List[String]]): Unit = {
    if (requestCount.isDefined) {
      val requests = 0 until requestCount.get map { i =>
        request.withId(i.toString).processConstants(getConfigCallback())
      }
      exportToBash(requests.toList, throttle)
    } else if (ids.isDefined) {
      val requests = ids.get.zipWithIndex.map { case (id, i) =>
        request.withId(id).processConstants(getConfigCallback())
      }
      exportToBash(requests, throttle)
    }
  }

  def startBulkRequest(request: CardinalRequest, throttle: Option[Long], requestCount: Option[Int], ids: Option[List[String]]): Unit = {
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

    val grid = new GridPane
    grid.setPadding(new Insets(10))
    grid.getStyleClass.addAll("plain-border", "round-border")
    grid.getRowConstraints.addAll(
      RowConstraintsBuilder().withVgrow(Priority.NEVER).build,
      RowConstraintsBuilder().withVgrow(Priority.NEVER).build,
      RowConstraintsBuilder().withVgrow(Priority.NEVER).build,
      RowConstraintsBuilder().withVgrow(Priority.NEVER).build
    )
    grid.getColumnConstraints.addAll(
      ColumnConstraintsBuilder().withPercentageWidth(100).build
    )
    grid.setHgap(10)
    grid.setVgap(10)

    val labelSendingRequest = new Label("Sending requests...")
    GridPane.setHalignment(labelSendingRequest, HPos.CENTER)
    GridPane.setHgrow(labelSendingRequest, Priority.ALWAYS)
    GridPane.setFillWidth(labelSendingRequest, true)
    grid.add(labelSendingRequest, 0, 0)

    val progressBar = new ProgressBar()
    progressBar.progressProperty().unbind()
    progressBar.progressProperty().bind(task.progressProperty())
    progressBar.setMaxWidth(java.lang.Double.MAX_VALUE)
    GridPane.setHgrow(progressBar, Priority.ALWAYS)
    grid.add(progressBar, 0, 1)

    GridPane.setHalignment(labelDelta, HPos.CENTER)
    GridPane.setHgrow(labelDelta, Priority.ALWAYS)
    GridPane.setFillWidth(labelDelta, true)
    grid.add(labelDelta, 0, 2)

    val buttonAbort = new Button("Abort")
    buttonAbort.setOnAction(_ => {
      task.cancel()
      finishedBulkRequestCallback(requestsAndResponses.toList, throttle)
    })
    GridPane.setHalignment(buttonAbort, HPos.CENTER)
    grid.add(buttonAbort, 0, 3)


    Platform.runLater(() => setCenter(grid))
    new Thread(task).start()
  }

  def finishedBulkRequestCallback(requestAndResponses: List[(CardinalRequest, Option[CardinalResponse])], throttle: Option[Long]): Unit = {
    Platform.runLater(() =>
      setCenter(CompletedBulkRequestOutput(requestAndResponses, exportToCsv, exportToBash, throttle))
    )
  }

}

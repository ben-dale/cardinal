package uk.co.ridentbyte.view.response

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.geometry.{HPos, Insets}
import javafx.scene.chart._
import javafx.scene.control._
import javafx.scene.layout._
import javafx.scene.paint.Color
import uk.co.ridentbyte.model.{BulkRequest, Config, CardinalResponse, CardinalRequest}
import uk.co.ridentbyte.view.dialog.BulkRequestInputDialog
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}

class ResponsePane(getConfigCallback: () => Config,
                   sendRequestCallback: CardinalRequest => CardinalResponse,
                   exportToCsv: Map[CardinalRequest, CardinalResponse] => Unit,
                   showErrorDialogCallback: String => Unit) extends BorderPane {

  setPadding(new Insets(20, 20, 20, 0))
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
    val grid = new GridPane
    grid.setVgap(10)
    grid.setHgap(10)

    val textAreaCurl = new TextArea()
    textAreaCurl.getStyleClass.add("cardinal-font")
    textAreaCurl.setText(command)
    textAreaCurl.setWrapText(true)
    GridPane.setHgrow(textAreaCurl, Priority.ALWAYS)
    GridPane.setVgrow(textAreaCurl, Priority.ALWAYS)
    grid.add(textAreaCurl, 0, 0)

    Platform.runLater(() => setCenter(grid))
  }

  def showBulkRequestInput(request: CardinalRequest, bulkRequest: Option[BulkRequest] = None): Unit = {
    val bulkRequestInputDialog = new BulkRequestInputDialog(bulkRequest)
    val bulkRequestResult = bulkRequestInputDialog.showAndWait()
    if (bulkRequestResult != null && bulkRequestResult.isPresent) {
      if (bulkRequestResult.get.throttle.isEmpty || (bulkRequestResult.get.count.isEmpty && bulkRequestResult.get.ids.isEmpty)) {
        showErrorDialogCallback("Invalid input. \nPlease provide a throttle and either a request count or a range value.")
        showBulkRequestInput(request, Some(bulkRequestResult.get))
      } else {
        startBulkRequest(request, bulkRequestResult.get.throttle, bulkRequestResult.get.count, bulkRequestResult.get.ids)
      }
    }
  }

  def startBulkRequest(request: CardinalRequest, throttle: Option[Long], requestCount: Option[Int], ids: Option[List[String]]): Unit = {
    val requestsAndResponses = collection.mutable.Map.empty[CardinalRequest, CardinalResponse]
    val labelDelta = new Label()

    val task = new Task[Boolean]() {
      override def call(): Boolean = {
        if (throttle.isDefined && requestCount.isDefined) {
          0 until requestCount.get foreach { i =>
            Thread.sleep(throttle.get)
            val r = request.withId(i.toString).processConstants(getConfigCallback())
            val response = sendRequestCallback(r)
            requestsAndResponses.put(r, response)
            updateProgress(i + 1, requestCount.get)
            Platform.runLater(() => {
              labelDelta.setText(requestsAndResponses.size.toString)
            })
          }
          finishedBulkRequestCallback(requestsAndResponses.toMap)
          true
        } else if (throttle.isDefined && ids.isDefined) {
          ids.get.zipWithIndex.foreach { case (id, i) =>
            Thread.sleep(throttle.get)
            val r = request.withId(id).processConstants(getConfigCallback())
            val response = sendRequestCallback(r)
            requestsAndResponses.put(r, response)
            updateProgress(i + 1, ids.get.length.toDouble)
            Platform.runLater(() => {
              labelDelta.setText(requestsAndResponses.size.toString)
            })
          }
          finishedBulkRequestCallback(requestsAndResponses.toMap)
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
      finishedBulkRequestCallback(requestsAndResponses.toMap)
    })
    GridPane.setHalignment(buttonAbort, HPos.CENTER)
    grid.add(buttonAbort, 0, 3)


    Platform.runLater(() => setCenter(grid))
    new Thread(task).start()
  }

  def finishedBulkRequestCallback(requestAndResponses: Map[CardinalRequest, CardinalResponse]): Unit = {
    val grid = new GridPane
    grid.getStyleClass.addAll("plain-border", "round-border")
    grid.setPadding(new Insets(15))
    grid.setHgap(15)
    grid.setVgap(15)

    grid.getRowConstraints.addAll(
      RowConstraintsBuilder().withPercentageHeight(70).withVgrow(Priority.ALWAYS).build,
      RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build,
      RowConstraintsBuilder().withVgrow(Priority.NEVER).build
    )

    val xAxis = new NumberAxis
    xAxis.setForceZeroInRange(true)
    xAxis.setMinorTickVisible(false)
    xAxis.setTickLabelsVisible(false)

    val yAxis = new NumberAxis
    yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, null, " ms"))
    yAxis.setForceZeroInRange(true)

    val timeSeries = new XYChart.Series[Number, Number]
    requestAndResponses.values.zipWithIndex.foreach { case(r, i) =>
      timeSeries.getData.add(new XYChart.Data(i, r.time))
    }

    val lineChart = new AreaChart[Number, Number](xAxis, yAxis)
    lineChart.setTitle("Response time over time")
    lineChart.setCreateSymbols(false)
    lineChart.setLegendVisible(false)
    lineChart.getData.add(timeSeries)
    GridPane.setHgrow(lineChart, Priority.ALWAYS)
    GridPane.setColumnSpan(lineChart, 2)
    grid.add(lineChart, 0, 0)

    val allTimes = requestAndResponses.values.map(_.time).toList
    val allTimesSorted = allTimes.sorted
    val averageResponseTime = allTimes.sum / (if (requestAndResponses.values.isEmpty) 1 else requestAndResponses.values.size)

    val responseCodesWithCounts = requestAndResponses.values.groupBy(_.raw.code).map { case (c, r) =>
      s"HTTP $c.................. ${r.size}"
    }
    val timings =
      s"""Timings
         |---
         |Average Response Time..... $averageResponseTime ms
         |Fastest Response Time..... ${allTimesSorted.headOption.getOrElse(0)} ms
         |Slowest Response Time..... ${allTimesSorted.lastOption.getOrElse(0)} ms
         |
         |Request/Response Counts
         |---
         |Total Responses........... ${requestAndResponses.size}
         |${responseCodesWithCounts.mkString("\n")}""".stripMargin

    val textAreaTimings = new TextArea(timings)
    textAreaTimings.getStyleClass.add("cardinal-font-console")
    GridPane.setHgrow(textAreaTimings, Priority.ALWAYS)
    grid.add(textAreaTimings, 0, 1)

    val exportButton = new Button("Export to CSV...")
    exportButton.setOnAction(_ => exportToCsv(requestAndResponses))
    GridPane.setHalignment(exportButton, HPos.CENTER)
    grid.add(exportButton, 0, 2)

    Platform.runLater(() => setCenter(grid))
  }

}

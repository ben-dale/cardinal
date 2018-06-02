package uk.co.ridentbyte.view.response

import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.concurrent.Task
import javafx.geometry.{HPos, Insets}
import javafx.scene.chart._
import javafx.scene.control._
import javafx.scene.layout._
import javafx.scene.paint.Color
import uk.co.ridentbyte.model.{BulkRequest, Config, HttpResponseWrapper, Request}
import uk.co.ridentbyte.view.dialog.BulkRequestInputDialog
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}

class ResponsePane(getConfigCallback: () => Config,
                   sendRequestCallback: Request => HttpResponseWrapper,
                   exportToCsv: Map[Request, HttpResponseWrapper] => Unit,
                   showErrorDialogCallback: String => Unit) extends BorderPane {

  setPadding(new Insets(20, 20, 20, 0))
  clearContents()

  def clearContents(): Unit = {
    val emptyPanel = new BorderPane
    emptyPanel.getStyleClass.addAll("dashed-border", "round-border")
    emptyPanel.setBorder(new Border(new BorderStroke(Color.LIGHTGREY, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)))
    setCenter(emptyPanel)
  }

  def setResponse(response: Option[HttpResponseWrapper]): Unit = {
    if (response.isDefined) {
      val grid = new GridPane
      grid.setVgap(10)
      grid.setHgap(10)
      grid.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build)
      grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).withPercentageHeight(40).build)
      grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).withPercentageHeight(60).build)

      val listHeaders = new ListView[String]()
      listHeaders.getStyleClass.add("cardinal-font")
      response.get.httpResponse.headers.foreach {
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

  def showBulkRequestInput(request: Request, bulkRequest: Option[BulkRequest] = None): Unit = {
    val bulkRequestInputDialog = new BulkRequestInputDialog(bulkRequest)
    val bulkRequestResult = bulkRequestInputDialog.showAndWait()
    if (bulkRequestResult != null && bulkRequestResult.isPresent) {
      if (bulkRequestResult.get.throttle.isEmpty || (bulkRequestResult.get.count.isEmpty && bulkRequestResult.get.ids.isEmpty)) {
        showErrorDialogCallback("Error bro")
        showBulkRequestInput(request, Some(bulkRequestResult.get))
      } else {
        startBulkRequest(request, bulkRequestResult.get.throttle, bulkRequestResult.get.count, bulkRequestResult.get.ids)
      }
    }
  }

  def startBulkRequest(request: Request, throttle: Option[Long], requestCount: Option[Int], ids: Option[List[String]]): Unit = {
    val requestsAndResponses = collection.mutable.Map.empty[Request, HttpResponseWrapper]
    val labelDelta = new Label()

    val task = new Task[Boolean]() {
      override def call(): Boolean = {
        if (throttle.isDefined && requestCount.isDefined) {
          1 to requestCount.get foreach { i =>
            Thread.sleep(throttle.get)
            val r = request.withId(i.toString).processConstants(getConfigCallback())
            val response = sendRequestCallback(r)
            requestsAndResponses.put(r, response)
            updateProgress(i, requestCount.get)
            Platform.runLater(() => labelDelta.setText(requestsAndResponses.size.toString))
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
            Platform.runLater(() => labelDelta.setText(requestsAndResponses.size.toString))
          }
          finishedBulkRequestCallback(requestsAndResponses.toMap)
          true
        } else {
          false
        }
      }
    }

    val grid = new GridPane
    grid.setHgap(10)
    grid.setVgap(10)
    grid.setMinWidth(400)

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

  def finishedBulkRequestCallback(requestAndResponses: Map[Request, HttpResponseWrapper]): Unit = {
    val grid = new GridPane
    grid.getStyleClass.addAll("plain-border", "round-border")
    grid.setPadding(new Insets(15))
    grid.setHgap(10)
    grid.setVgap(10)

    grid.getRowConstraints.addAll(
      RowConstraintsBuilder().withPercentageHeight(70).withVgrow(Priority.ALWAYS).build,
      RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build,
      RowConstraintsBuilder().withVgrow(Priority.NEVER).build
    )

    val httpResponseData: ObservableList[PieChart.Data] = FXCollections.observableArrayList()
    val allHttpCodes = requestAndResponses.map{ case (_, res) =>
      res.httpResponse.code
    }.toList.distinct

    allHttpCodes.foreach { httpCode =>
      val count = requestAndResponses.values.count(_.httpResponse.code == httpCode)
      val datum = new PieChart.Data("HTTP " + httpCode, count)
      httpResponseData.add(datum)
    }

    val pieChart = new PieChart(httpResponseData)
    pieChart.setLegendVisible(false)
    GridPane.setHalignment(pieChart, HPos.CENTER)
    GridPane.setHgrow(pieChart, Priority.ALWAYS)
    GridPane.setColumnSpan(pieChart, 2)
    grid.add(pieChart, 0, 0)

    val allTimes = requestAndResponses.values.map(_.time).toList
    val allTimesSorted = allTimes.sorted
    val averageResponseTime = allTimes.sum / requestAndResponses.values.size

    val timings =
      s"""Timings
         |---
         |Average Response Time..... $averageResponseTime ms
         |Fastest Response Time..... ${allTimesSorted.head} ms
         |Slowest Response Time..... ${allTimesSorted.last} ms""".stripMargin

    val textAreaTimings = new TextArea(timings)
    textAreaTimings.getStyleClass.add("cardinal-font-console")
    GridPane.setHalignment(textAreaTimings, HPos.CENTER)
    GridPane.setHgrow(textAreaTimings, Priority.ALWAYS)
    GridPane.setColumnSpan(textAreaTimings, 1)
    grid.add(textAreaTimings, 0, 1)

    val responseCodesWithCounts = requestAndResponses.values.groupBy(_.httpResponse.code).map { case (c, r) =>
      s"HTTP $c.......... ${r.size}"
    }

    val responseCounts =
      s"""Request/Response Counts
        |---
        |Total Responses... ${requestAndResponses.size}
        |${responseCodesWithCounts.mkString("\n")}""".stripMargin

    val textAreaRequestResponseCounts = new TextArea(responseCounts)
    textAreaRequestResponseCounts.getStyleClass.add("cardinal-font-console")
    GridPane.setHalignment(textAreaRequestResponseCounts, HPos.CENTER)
    GridPane.setHgrow(textAreaRequestResponseCounts, Priority.ALWAYS)
    GridPane.setColumnSpan(textAreaRequestResponseCounts, 1)
    grid.add(textAreaRequestResponseCounts, 1, 1)

    val exportButton = new Button("Export to CSV...")
    exportButton.setOnAction(_ => exportToCsv(requestAndResponses))
    GridPane.setHalignment(exportButton, HPos.CENTER)
    GridPane.setColumnSpan(exportButton, 2)
    grid.add(exportButton, 0, 2)

    Platform.runLater(() => setCenter(grid))
  }

}

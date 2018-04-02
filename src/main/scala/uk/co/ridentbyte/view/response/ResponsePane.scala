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
                   sendRequestCallback: (Request) => HttpResponseWrapper,
                   showErrorDialogCallback: (String) => Unit) extends BorderPane {

  setPadding(new Insets(20, 20, 20, 0))
  clearContents()

  def clearContents(): Unit = {
    val emptyPanel = new BorderPane
    emptyPanel.getStyleClass.addAll("dashed-border", "round-border")
    emptyPanel.setBorder(new Border(new BorderStroke(Color.LIGHTGREY, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)))
    val label = new Label("Cardinal 0.1")
    label.getStyleClass.add("cardinal-font")
    emptyPanel.setCenter(label)
    setCenter(emptyPanel)
  }

  def setResponse(response: HttpResponseWrapper): Unit = {
    val grid = new GridPane
//    grid.setPadding(new Insets(20, 20, 20, 0))
    grid.setVgap(10)
    grid.setHgap(10)
    grid.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build)
    grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).withPercentageHeight(40).build)
    grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).withPercentageHeight(60).build)

    val listHeaders = new ListView[String]()
    listHeaders.setStyle(
      """
        |-fx-font-family: Monospaced;
        |-fx-font-size: 13;
        |-fx-font-weight: 600;
      """.stripMargin
    )
    response.httpResponse.headers.foreach {
      header => listHeaders.getItems.add(header._1 + ": " + header._2.mkString(""))
    }
    grid.add(listHeaders, 0, 0)

    val textAreaBody = new TextArea()
    textAreaBody.setEditable(false)
    textAreaBody.setStyle(
      """
        |-fx-font-family: Monospaced;
        |-fx-font-size: 13;
        |-fx-font-weight: 600;
      """.stripMargin
    )
    textAreaBody.setText(response.formattedBody)
    grid.add(textAreaBody, 0, 1)

    Platform.runLater(() => setCenter(grid))
  }

  def loadCurlCommand(command: String): Unit = {
    val grid = new GridPane
//    grid.setPadding(new Insets(10, 10, 10, 10))
    grid.setVgap(10)
    grid.setHgap(10)

    val textAreaCurl = new TextArea()
    textAreaCurl.setStyle(
      """
        |-fx-font-family: Monospaced;
        |-fx-font-size: 13;
        |-fx-font-weight: 600;
      """.stripMargin
    )
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
    var allResponses = collection.mutable.ListBuffer.empty[HttpResponseWrapper]

    val labelDelta = new Label()

    val task = new Task[Boolean]() {
      override def call(): Boolean = {
        if (throttle.isDefined && requestCount.isDefined) {
          1 to requestCount.get foreach { i =>
            Thread.sleep(throttle.get)
            val r = request.withId(i.toString).processConstants(getConfigCallback())
            val response = sendRequestCallback(r)
            allResponses += response
            updateProgress(i, requestCount.get)
            Platform.runLater(() => labelDelta.setText(allResponses.length.toString))
          }
          finishedBulkRequestCallback(allResponses.toList)
          true
        } else if (throttle.isDefined && ids.isDefined) {
          ids.get.zipWithIndex.foreach { case (id, i) =>
            Thread.sleep(throttle.get)
            val r = request.withId(id).processConstants(getConfigCallback())
            val response = sendRequestCallback(r)
            allResponses += response
            updateProgress(i + 1, ids.get.length.toDouble)
            Platform.runLater(() => labelDelta.setText(allResponses.length.toString))
          }
          finishedBulkRequestCallback(allResponses.toList)
          true
        } else {
          false
        }
      }
    }

    val grid = new GridPane
//    grid.setPadding(new Insets(10, 10, 10, 10))
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
    buttonAbort.setOnAction((_) => {
      task.cancel()
      finishedBulkRequestCallback(allResponses.toList)
    })
    GridPane.setHalignment(buttonAbort, HPos.CENTER)
    grid.add(buttonAbort, 0, 3)


    Platform.runLater(() => setCenter(grid))
    new Thread(task).start()
  }

  def finishedBulkRequestCallback(responses: List[HttpResponseWrapper]): Unit = {
    val grid = new GridPane
//    grid.setPadding(new Insets(10, 10, 10, 10))
    grid.setHgap(10)
    grid.setVgap(10)
    grid.setMinWidth(500)

    val timeSeries = new XYChart.Series[String, Number]
    responses.zipWithIndex.foreach { case(r, i) =>
      timeSeries.getData.add(new XYChart.Data((i + 1).toString, r.time))
    }

    val httpResponseData: ObservableList[PieChart.Data] = FXCollections.observableArrayList()
    val allHttpCodes = responses.map(_.httpResponse.code).distinct
    allHttpCodes.foreach { httpCode =>
      val count = responses.count(_.httpResponse.code == httpCode)
      httpResponseData.add(new PieChart.Data("HTTP " + httpCode, count))
    }

    val xAxis = new CategoryAxis
    xAxis.setLabel("Request")

    val yAxis = new NumberAxis
    yAxis.setLabel("Time (ms)")

    val pieChart = new PieChart(httpResponseData)
    pieChart.setLegendVisible(false)
    pieChart.setLabelsVisible(true)
    pieChart.setMaxHeight(200)
    GridPane.setHalignment(pieChart, HPos.CENTER)
    GridPane.setHgrow(pieChart, Priority.ALWAYS)
    grid.add(pieChart, 0, 0)

    val lineChart = new LineChart[String, Number](xAxis, yAxis)
    lineChart.setCreateSymbols(false)
    lineChart.setLegendVisible(false)
    lineChart.getData.add(timeSeries)
    lineChart.setMaxHeight(150)
    GridPane.setHgrow(lineChart, Priority.ALWAYS)
    grid.add(lineChart, 0, 1)

    Platform.runLater(() => setCenter(grid))
  }



}

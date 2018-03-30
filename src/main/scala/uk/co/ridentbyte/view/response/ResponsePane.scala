package uk.co.ridentbyte.view.response

import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.concurrent.Task
import javafx.geometry.{HPos, Insets}
import javafx.scene.chart._
import javafx.scene.control._
import javafx.scene.layout._
import uk.co.ridentbyte.model.{HttpResponseWrapper, Request}
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}

class ResponsePane(sendRequestCallback: (Request) => HttpResponseWrapper) extends BorderPane {

  def clearContents(): Unit = {
    setCenter(null)
  }

  def setResponse(response: HttpResponseWrapper): Unit = {
    val grid = new GridPane
    grid.setPadding(new Insets(10, 10, 10, 10))
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

  def setCurlCommand(command: String): Unit = {
    val grid = new GridPane
    grid.setPadding(new Insets(10, 10, 10, 10))
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

  def showBulkRequestInput(request: Request): Unit = {
    val grid = new GridPane
    grid.setPadding(new Insets(10, 10, 10, 10))
    grid.setHgap(10)
    grid.setVgap(10)

    val labelDelay = new Label("Delay per request (ms)")
    GridPane.setHalignment(labelDelay, HPos.RIGHT)
    grid.add(labelDelay, 0, 0)

    val textDelay = new TextField
    GridPane.setHgrow(textDelay, Priority.ALWAYS)
    textDelay.setText("500")
    grid.add(textDelay, 1, 0)

    val labelNumOfRequests = new Label("No. of requests")
    GridPane.setHalignment(labelNumOfRequests, HPos.RIGHT)
    grid.add(labelNumOfRequests, 0, 1)

    val textNumOfRequests = new TextField
    GridPane.setHgrow(textNumOfRequests, Priority.ALWAYS)
    grid.add(textNumOfRequests, 1, 1)

    val labelOr = new Label("- OR -")
    GridPane.setColumnSpan(labelOr, 3)
    GridPane.setHalignment(labelOr, HPos.CENTER)
    grid.add(labelOr, 0, 2)

    val labelForEach = new Label("For each")
    GridPane.setHalignment(labelForEach, HPos.RIGHT)
    grid.add(labelForEach, 0, 3)

    val textForEach = new TextField
    GridPane.setHgrow(textForEach, Priority.ALWAYS)
    textForEach.setPromptText("325, 454, 432 or 12..54")
    grid.add(textForEach, 1, 3)

    val buttonStart = new Button("Start")
    buttonStart.setOnAction((_) => {
      val optTextDelay = if (textDelay.getText.trim.length == 0) {
        None
      } else {
        try {
          Some(textDelay.getText.trim.toLong)
        } catch {
          case _: Exception => None
        }
      }

      val optNumRequests = if (textNumOfRequests.getText.trim.length == 0) {
        None
      } else {
        try {
          Some(textNumOfRequests.getText.trim.toInt)
        } catch {
          case _: Exception => None
        }
      }

      val optTextForEach = if (textForEach.getText.trim.length == 0) {
        None
      } else {
        val matcher = "([0-9]+)..([0-9]+)".r
        textForEach.getText.trim match {
          case matcher(a, b) => Some(a.toInt.to(b.toInt).map(_.toString).toList)
          case v => Some(v.split(",").toList)
        }
      }
      startBulkRequest(request, optTextDelay, optNumRequests, optTextForEach)
    })
    GridPane.setHalignment(buttonStart, HPos.RIGHT)
    grid.add(buttonStart, 1, 4)

    val labelVerb = new Label(request.verb)
    grid.add(labelVerb, 0, 5)

    val labelURI = new Label(request.uri)
    grid.add(labelURI, 1, 5)

    val labelHeaders = new Label("Headers")
    GridPane.setColumnSpan(labelHeaders, 2)
    grid.add(labelHeaders, 0, 6)

    val listHeaders = new ListView[String]()
    listHeaders.setStyle(
      """
        |-fx-font-family: Monospaced;
        |-fx-font-size: 13;
        |-fx-font-weight: 600;
      """.stripMargin
    )
    listHeaders.setEditable(false)
    listHeaders.setMaxHeight(200)
    request.headers.foreach { header =>
      listHeaders.getItems.add(header)
    }
    GridPane.setColumnSpan(listHeaders, 2)
    grid.add(listHeaders, 0, 7)

    val labelBodyExample = new Label("Body (Example)")
    GridPane.setColumnSpan(labelBodyExample, 2)
    grid.add(labelBodyExample, 0, 8)

    val textAreaExampleBody = new TextArea()
    textAreaExampleBody.setStyle(
      """
        |-fx-font-family: Monospaced;
        |-fx-font-size: 13;
        |-fx-font-weight: 600;
      """.stripMargin
    )
    textAreaExampleBody.setEditable(false)
    textAreaExampleBody.setText(request.processConstants().body.getOrElse(""))
    GridPane.setColumnSpan(textAreaExampleBody, 2)
    textAreaExampleBody.setEditable(false)
    grid.add(textAreaExampleBody, 0, 9)

    Platform.runLater(() => setCenter(grid))
  }

  def startBulkRequest(request: Request, throttle: Option[Long], requestCount: Option[Int], ids: Option[List[String]]): Unit = {
    var allResponses = collection.mutable.ListBuffer.empty[HttpResponseWrapper]

    val labelDelta = new Label()

    val task = new Task[Boolean]() {
      override def call(): Boolean = {
        if (throttle.isDefined && requestCount.isDefined) {
          1 to requestCount.get foreach { i =>
            Thread.sleep(throttle.get)
            val r = request.withId(i.toString).processConstants()
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
            val r = request.withId(id).processConstants()
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
    grid.setPadding(new Insets(10, 10, 10, 10))
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
    grid.setPadding(new Insets(10, 10, 10, 10))
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

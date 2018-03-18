package uk.co.ridentbyte.view.response

import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.concurrent.Task
import javafx.geometry.{HPos, Insets}
import javafx.scene.chart._
import javafx.scene.control._
import javafx.scene.layout.{GridPane, Priority}
import uk.co.ridentbyte.model.{HttpResponseWrapper, Request}
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}

class ResponsePane(sendRequestCallback: (Request) => HttpResponseWrapper) extends GridPane {

  private val tabbedPane = new TabPane()
  GridPane.setVgrow(tabbedPane, Priority.ALWAYS)
  GridPane.setHgrow(tabbedPane, Priority.ALWAYS)
  add(tabbedPane, 0, 1)

  tabbedPane.getTabs.add(infoTab)

  def clear(): Unit = {
    tabbedPane.getTabs.remove(1, tabbedPane.getTabs.size())
  }

  def addResponse(response: HttpResponseWrapper): Unit = {
    val newTab = new Tab("HTTP " + response.httpResponse.code)

    val grid = new GridPane
    grid.setPadding(new Insets(10, 10, 10, 10))
    grid.setVgap(10)
    grid.setHgap(10)
    grid.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build)
    grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).withPercentageHeight(55).build)
    grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).withPercentageHeight(45).build)

    val textAreaBody = new TextArea()
    textAreaBody.setEditable(false)
    textAreaBody.setStyle(
      """
        |-fx-font-family: Monospaced;
        |-fx-font-size: 13;
        |-fx-font-weight: 600;
      """.stripMargin
    )
    textAreaBody.setText(response.httpResponse.body)
    grid.add(textAreaBody, 0, 0)

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
    grid.add(listHeaders, 0, 1)

    newTab.setContent(grid)
    tabbedPane.getTabs.add(newTab)
    tabbedPane.getSelectionModel.select(newTab)
  }

  def infoTab: Tab = {
    val tab = new Tab("Info")
    tab.setClosable(false)
    tab
  }

  def showBulkRequestInput(request: Request): Unit = {
    val bulkRequestTab = bulkRequestInput(request)
    tabbedPane.getTabs.add(bulkRequestTab)
    tabbedPane.getSelectionModel.select(bulkRequestTab)
  }

  def bulkRequestInput(request: Request): Tab = {
    val tab = new Tab("Bulk Request")

    val grid = new GridPane
    grid.setPadding(new Insets(10, 10, 10, 10))
    grid.setHgap(10)
    grid.setVgap(10)

    val labelVerb = new Label(request.verb)
    grid.add(labelVerb, 0, 0)

    val labelURI = new Label(request.uri)
    grid.add(labelURI, 1, 0)

    val labelHeaders = new Label("Headers")
    GridPane.setColumnSpan(labelHeaders, 2)
    grid.add(labelHeaders, 0, 1)

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
    grid.add(listHeaders, 0, 2)

    val labelBodyExample = new Label("Body (Example)")
    GridPane.setColumnSpan(labelBodyExample, 2)
    grid.add(labelBodyExample, 0, 3)

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
    grid.add(textAreaExampleBody, 0, 4)

    val labelDelay = new Label("Delay per request (ms)")
    GridPane.setHalignment(labelDelay, HPos.RIGHT)
    grid.add(labelDelay, 0, 5)

    val textDelay = new TextField
    GridPane.setHgrow(textDelay, Priority.ALWAYS)
    textDelay.setText("500")
    grid.add(textDelay, 1, 5)

    val labelNumOfRequests = new Label("No. of requests")
    GridPane.setHalignment(labelNumOfRequests, HPos.RIGHT)
    grid.add(labelNumOfRequests, 0, 6)

    val textNumOfRequests = new TextField
    GridPane.setHgrow(textNumOfRequests, Priority.ALWAYS)
    grid.add(textNumOfRequests, 1, 6)

    val labelOr = new Label("- OR -")
    GridPane.setColumnSpan(labelOr, 3)
    GridPane.setHalignment(labelOr, HPos.CENTER)
    grid.add(labelOr, 0, 7)

    val labelForEach = new Label("For each")
    GridPane.setHalignment(labelForEach, HPos.RIGHT)
    grid.add(labelForEach, 0, 8)

    val textForEach = new TextField
    GridPane.setHgrow(textForEach, Priority.ALWAYS)
    textForEach.setPromptText("325, 454, 432 or 12..54")
    grid.add(textForEach, 1, 8)

    tab.setContent(grid)

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
      startBulkRequestInTab(tab, request, optTextDelay, optNumRequests, optTextForEach)
    })
    GridPane.setHalignment(buttonStart, HPos.RIGHT)
    grid.add(buttonStart, 1, 9)


    tab
  }

  def startBulkRequestInTab(tab: Tab, request: Request, throttle: Option[Long], requestCount: Option[Int], ids: Option[List[String]]): Unit = {

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
          finishedBulkRequestCallback(tab, allResponses.toList)
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
          finishedBulkRequestCallback(tab, allResponses.toList)
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
    GridPane.setFillWidth(labelSendingRequest, true)
    grid.add(labelSendingRequest, 0, 0)

    val progressBar = new ProgressBar()
    progressBar.progressProperty().unbind()
    progressBar.progressProperty().bind(task.progressProperty())
    progressBar.setPrefWidth(400)
    GridPane.setFillWidth(progressBar, true)
    grid.add(progressBar, 0, 1)

    GridPane.setHalignment(labelDelta, HPos.CENTER)
    GridPane.setFillWidth(labelDelta, true)
    grid.add(labelDelta, 0, 2)

    Platform.runLater(() => tab.setContent(grid))

    new Thread(task).start()
  }

  def finishedBulkRequestCallback(tab: Tab, responses: List[HttpResponseWrapper]): Unit = {
    val grid = new GridPane
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

    Platform.runLater(() => tab.setContent(grid))
  }

}




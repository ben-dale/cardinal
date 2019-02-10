package uk.co.ridentbyte.view.response

import javafx.geometry.Insets
import javafx.scene.chart.{AreaChart, NumberAxis, XYChart}
import javafx.scene.control.{Button, TextArea}
import javafx.scene.layout.{GridPane, Priority}
import uk.co.ridentbyte.model.{CardinalRequest, CardinalRequestAndResponse}
import uk.co.ridentbyte.view.util.RowConstraintsBuilder
import scala.collection.JavaConverters._

case class BulkRequestOutputPane(requestAndResponses: List[CardinalRequestAndResponse],
                                 exportToCsv: java.util.function.Function[java.util.List[CardinalRequestAndResponse], Void],
                                 exportToBash: java.util.function.BiFunction[java.util.List[CardinalRequest], java.lang.Integer, Void],
                                 throttle: Int) extends GridPane {

  val responses = requestAndResponses.map(_.getResponse).filter(_ != null)

  setHgap(10)
  setVgap(10)
  setPadding(new Insets(20))
  getStyleClass.addAll("plain-border", "round-border")

  getRowConstraints.addAll(
    new RowConstraintsBuilder().withPercentageHeight(55).withVgrow(Priority.ALWAYS).build,
    new RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build,
    new RowConstraintsBuilder().withVgrow(Priority.NEVER).build
  )

  val xAxis = new NumberAxis
  xAxis.setForceZeroInRange(true)
  xAxis.setMinorTickVisible(false)
  xAxis.setTickLabelsVisible(false)

  val yAxis = new NumberAxis
  yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, null, " ms"))
  yAxis.setForceZeroInRange(true)

  val timeSeries = new XYChart.Series[Number, Number]
  responses.zipWithIndex.foreach { case(r, i) =>
    timeSeries.getData.add(new XYChart.Data(i, r.getTime))
  }

  val lineChart = new AreaChart[Number, Number](xAxis, yAxis)
  lineChart.setTitle("Response time over time")
  lineChart.setCreateSymbols(false)
  lineChart.setLegendVisible(false)
  lineChart.getData.add(timeSeries)
  GridPane.setHgrow(lineChart, Priority.ALWAYS)
  GridPane.setColumnSpan(lineChart, 2)
  add(lineChart, 0, 0)

  val allTimes = responses.map(_.getTime)
  val allTimesSorted = allTimes.sorted
  val averageResponseTime = if (responses.isEmpty) 0 else allTimes.sum / responses.size

  val responseCodesWithCounts = responses.groupBy(_.getStatusCode)

  val requestCountsOutput = if (responseCodesWithCounts.isEmpty) {
    List("No responses")
  } else {
    responseCodesWithCounts.map { case (c, r) =>
      s"HTTP $c.................. ${r.size}"
    }
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
       |${requestCountsOutput.mkString("\n")}""".stripMargin

  val textAreaTimings = new TextArea(timings)
  textAreaTimings.getStyleClass.add("cardinal-font-console")
  GridPane.setHgrow(textAreaTimings, Priority.ALWAYS)
  GridPane.setColumnSpan(textAreaTimings, 2)
  add(textAreaTimings, 0, 1)

  val exportToCsvButton = new Button("Export to CSV...")
  exportToCsvButton.setOnAction(_ => exportToCsv.apply(requestAndResponses.asJava))
  GridPane.setColumnSpan(exportToCsvButton, 1)
  GridPane.setHgrow(exportToCsvButton, Priority.NEVER)
  add(exportToCsvButton, 0, 2)

  val exportToBashButton = new Button("Export as script...")
  exportToBashButton.setOnAction(_ => exportToBash.apply(requestAndResponses.map(_.getRequest).asJava, throttle))
  GridPane.setColumnSpan(exportToBashButton, 1)
  GridPane.setHgrow(exportToBashButton, Priority.ALWAYS)
  add(exportToBashButton, 1, 2)
}

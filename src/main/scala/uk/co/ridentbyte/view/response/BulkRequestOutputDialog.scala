package uk.co.ridentbyte.view.response

import javafx.scene.chart._
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control._
import javafx.scene.layout.{GridPane, Priority}
import uk.co.ridentbyte.model.HttpResponseWrapper

class BulkRequestOutputDialog(responses: List[HttpResponseWrapper]) extends Dialog[Void] {

  setTitle("Bulk Request Results")

  val grid = new GridPane
  grid.setHgap(10)
  grid.setVgap(10)
  grid.setMinWidth(500)


  private val timeSeries = new XYChart.Series[String, Number]
  responses.zipWithIndex.foreach { case(r, i) =>
    timeSeries.getData.add(new XYChart.Data((i + 1).toString, r.time))
  }

  val xAxis = new CategoryAxis
  xAxis.setLabel("Request")

  val yAxis = new NumberAxis
  yAxis.setLabel("Time (ms)")

  val lineChart = new LineChart[String, Number](xAxis, yAxis)
  lineChart.setCreateSymbols(false)
  lineChart.setLegendVisible(false)
  lineChart.getData.add(timeSeries)
  lineChart.setMaxHeight(200)
  GridPane.setHgrow(lineChart, Priority.ALWAYS)
  grid.add(lineChart, 0, 1)

  getDialogPane.setContent(grid)
  getDialogPane.getButtonTypes.addAll(new ButtonType("Close", ButtonData.CANCEL_CLOSE))
}

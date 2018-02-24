package uk.co.ridentbyte.view.response

import javafx.geometry.{HPos, Insets}
import javafx.scene.control.Label
import javafx.scene.layout.GridPane

import uk.co.ridentbyte.model.Header

class ResponseSummaryPane extends GridPane {

  setPadding(new Insets(10, 10, 10, 10))
  setHgap(10)

  val labelHttpCode = new Label()
  GridPane.setHalignment(labelHttpCode, HPos.LEFT)
  add(labelHttpCode, 0, 0)

  val labelRequestTiming = new Label()
  GridPane.setHalignment(labelRequestTiming, HPos.RIGHT)
  add(labelRequestTiming, 1, 0)

  def setHttpCode(code: String): Unit = {
    labelHttpCode.setText(code)
  }

  def setTime(time: Long): Unit = {
    labelRequestTiming.setText(time + " ms")
  }

  def clear(): Unit = {
    labelHttpCode.setText("")
    labelRequestTiming.setText("")
  }

}

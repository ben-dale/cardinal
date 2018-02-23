package uk.co.ridentbyte.view.response

import javafx.geometry.{HPos, Insets}
import javafx.scene.control.Label
import javafx.scene.layout.GridPane

import uk.co.ridentbyte.view.util.GridConstraints

class ResponseSummaryPane extends GridPane {

  setGridLinesVisible(true)
  setPadding(new Insets(10, 10, 10, 10))
  getColumnConstraints.add(GridConstraints.maxWidthColumnConstraint)
  getRowConstraints.add(GridConstraints.maxHeightRowConstraint)

  val labelHttpCode = new Label()
  labelHttpCode.setStyle(
    """
      |-fx-font-family: Monospaced;
      |-fx-font-size: 13;
      |-fx-font-weight: 900;
      |-fx-border-radius:20;
      |-fx-border-color:#494039;
    """.stripMargin)
  GridPane.setHalignment(labelHttpCode, HPos.LEFT)
  add(labelHttpCode, 0, 0)

  val labelRequestTiming = new Label()
  GridPane.setHalignment(labelRequestTiming, HPos.RIGHT)
  add(labelRequestTiming, 1, 0)

  def setHttpCode(code: String): Unit = {
    labelHttpCode.setText(code)
  }

}

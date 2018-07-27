package uk.co.ridentbyte.view.response

import javafx.scene.layout._
import javafx.scene.paint.Color

case class ClearResponsePane() extends BorderPane {
  getStyleClass.addAll("dashed-border", "round-border")
  setBorder(new Border(new BorderStroke(Color.LIGHTGREY, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)))
}

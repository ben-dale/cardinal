package uk.co.ridentbyte.view

import javafx.geometry.{HPos, VPos}
import javafx.scene.control.{Label, Tab}
import javafx.scene.layout.GridPane

case class CardinalInfoTab() extends Tab {

  val grid = new GridPane
  grid.getStyleClass.add("info")

  val label = new Label("Cardinal 0.1 Alpha")
  GridPane.setHalignment(label, HPos.CENTER)
  GridPane.setValignment(label, VPos.CENTER)
  grid.add(label, 0, 0)

  setContent(grid)
  setClosable(false)
  setText("Info")

}

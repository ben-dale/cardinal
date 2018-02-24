package uk.co.ridentbyte.view.util

import javafx.scene.layout.{ColumnConstraints, Priority, RowConstraints}

object GridConstraints {

  def maxHeightRowConstraint: RowConstraints = {
    val row = new RowConstraints()
    row.setVgrow(Priority.ALWAYS)
    row
  }

  def widthColumnConstraint(percentage: Int): ColumnConstraints = {
    val column = new ColumnConstraints()
    column.setPercentWidth(percentage)
    column.setHgrow(Priority.ALWAYS)
    column
  }

}

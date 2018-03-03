package uk.co.ridentbyte.view.util

import javafx.scene.layout.{ColumnConstraints, Priority}

case class ColumnConstraintsBuilder() {

  val columnConstraints = new ColumnConstraints

  def withHgrow(priority: Priority): ColumnConstraintsBuilder = {
    columnConstraints.setHgrow(priority)
    this
  }

  def withPercentageWidth(percentage: Int): ColumnConstraintsBuilder = {
    columnConstraints.setPercentWidth(percentage)
    this
  }

  def build: ColumnConstraints = columnConstraints

}

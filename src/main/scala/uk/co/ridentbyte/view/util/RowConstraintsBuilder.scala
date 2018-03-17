package uk.co.ridentbyte.view.util

import javafx.scene.layout.{Priority, RowConstraints}

case class RowConstraintsBuilder() {

  private val rowConstraints = new RowConstraints

  def withVgrow(priority: Priority): RowConstraintsBuilder = {
    rowConstraints.setVgrow(priority)
    this
  }

  def withMaxHeight(maxHeight: Int): RowConstraintsBuilder = {
    rowConstraints.setMaxHeight(maxHeight)
    this
  }

  def withPercentageHeight(percentage: Int): RowConstraintsBuilder = {
    rowConstraints.setPercentHeight(percentage)
    this
  }

  def build: RowConstraints = rowConstraints

}

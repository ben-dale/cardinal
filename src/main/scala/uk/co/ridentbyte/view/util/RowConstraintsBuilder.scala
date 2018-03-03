package uk.co.ridentbyte.view.util

import javafx.scene.layout.{Priority, RowConstraints}

case class RowConstraintsBuilder() {

  val rowConstraints = new RowConstraints

  def withVgrow(priority: Priority): RowConstraintsBuilder = {
    rowConstraints.setVgrow(priority)
    this
  }

  def build: RowConstraints = rowConstraints

}

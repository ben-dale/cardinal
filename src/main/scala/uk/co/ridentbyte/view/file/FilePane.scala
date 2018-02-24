package uk.co.ridentbyte.view.file

import javafx.geometry.Insets
import javafx.scene.control.ListView
import javafx.scene.layout._
import javafx.scene.paint.Color

class FilePane extends GridPane {

  setHgap(10)
  setVgap(10)
  setPadding(new Insets(10, 10, 10, 10))

  setBackground(new Background(new BackgroundFill(Color.web("#DDDDDD"), CornerRadii.EMPTY, Insets.EMPTY)))

  val listFiles = new ListView[String]
  GridPane.setHgrow(listFiles, Priority.ALWAYS)
  GridPane.setVgrow(listFiles, Priority.ALWAYS)
  add(listFiles, 0, 0)

}

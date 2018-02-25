package uk.co.ridentbyte.view.file

import java.io.File
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.geometry.Insets
import javafx.scene.control.ListView
import javafx.scene.layout._
import javafx.scene.paint.Color

class FilePane(loadFileCallback: (String) => Unit) extends GridPane {

  setHgap(10)
  setVgap(10)
  setPadding(new Insets(10, 10, 10, 10))

  setBackground(new Background(new BackgroundFill(Color.web("#DDDDDD"), CornerRadii.EMPTY, Insets.EMPTY)))

  val listFiles = new ListView[String]
  listFiles.getSelectionModel.selectedItemProperty().addListener(new ChangeListener[String] {
    override def changed(observable: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
      if (newValue != null) {
        loadFileCallback(listFiles.getSelectionModel.getSelectedItem)
      }
    }
  })
  GridPane.setHgrow(listFiles, Priority.ALWAYS)
  GridPane.setVgrow(listFiles, Priority.ALWAYS)
  add(listFiles, 0, 0)


  def loadFiles(files: List[File]): Unit = {
    listFiles.getItems.clear()
    files.foreach { file =>
      listFiles.getItems.add(file.getName)
    }
  }

}

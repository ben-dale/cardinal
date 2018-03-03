package uk.co.ridentbyte.view.file

import java.io.File
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.geometry.{HPos, Insets}
import javafx.scene.control.{Button, ListView}
import javafx.scene.layout._

class FilePane(loadFileCallback: (String) => Unit,
               deleteFileCallback: (String) => Unit,
               duplicateFileCallback: (String) => Unit) extends GridPane {

  setHgap(5)
  setVgap(5)
  setPadding(new Insets(10, 10, 10, 10))

  private val listFiles = new ListView[String]
  listFiles.getSelectionModel.selectedItemProperty().addListener(new ChangeListener[String] {
    override def changed(observable: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
      if (newValue != null) {
        loadFileCallback(listFiles.getSelectionModel.getSelectedItem + ".json")
      }
    }
  })
  GridPane.setHgrow(listFiles, Priority.ALWAYS)
  GridPane.setVgrow(listFiles, Priority.ALWAYS)
  GridPane.setColumnSpan(listFiles, 2)
  add(listFiles, 0, 0)

  private val buttonDelete = new Button("Delete")
  buttonDelete.setStyle("-fx-padding: 2 6 2 6;")
  buttonDelete.setOnAction((_) => {
    val selectedIndex = listFiles.getSelectionModel.getSelectedIndex
    if (selectedIndex >= 0) {
      deleteFileCallback(listFiles.getSelectionModel.getSelectedItem + ".json")
    }
  })
  GridPane.setHgrow(buttonDelete, Priority.ALWAYS)
  GridPane.setVgrow(buttonDelete, Priority.NEVER)
  GridPane.setHalignment(buttonDelete, HPos.RIGHT)
  add(buttonDelete, 0, 1)

  private val buttonDuplicate = new Button("Duplicate")
  buttonDuplicate.setStyle("-fx-padding: 2 6 2 6;")
  buttonDuplicate.setOnAction((_) => {
    val selectedIndex = listFiles.getSelectionModel.getSelectedIndex
    if (selectedIndex >= 0) {
      duplicateFileCallback(listFiles.getSelectionModel.getSelectedItem + ".json")
    }
  })
  GridPane.setHgrow(buttonDuplicate, Priority.NEVER)
  GridPane.setVgrow(buttonDuplicate, Priority.NEVER)
  add(buttonDuplicate, 1, 1)

  def loadFiles(files: List[File]): Unit = {
    listFiles.getItems.clear()
    files.foreach { file =>
      listFiles.getItems.add(file.getName.replaceFirst(".json", ""))
    }
  }

}

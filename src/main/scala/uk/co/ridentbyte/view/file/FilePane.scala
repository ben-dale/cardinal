package uk.co.ridentbyte.view.file

import java.io.File
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.geometry.{HPos, Insets}
import javafx.scene.control.{Button, ListView}
import javafx.scene.layout._

class FilePane(loadFileCallback: (String) => Unit, deleteFileCallback: (String) => Unit) extends GridPane {

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

  private val buttonRemove = new Button("-")
  buttonRemove.setStyle("-fx-padding: 2 6 2 6;")
  buttonRemove.setOnAction((_) => {
    val selectedIndex = listFiles.getSelectionModel.getSelectedIndex
    println(selectedIndex)
    println(listFiles.getFocusModel.getFocusedIndex)
    if (selectedIndex >= 0) {
      deleteFileCallback(listFiles.getSelectionModel.getSelectedItem + ".json")
    }
  })
  GridPane.setHgrow(buttonRemove, Priority.ALWAYS)
  GridPane.setVgrow(buttonRemove, Priority.NEVER)
  GridPane.setHalignment(buttonRemove, HPos.RIGHT)
  add(buttonRemove, 0, 1)

  private val buttonAdd = new Button("+")
  buttonAdd.setStyle("-fx-padding: 2 6 2 6;")
  GridPane.setHgrow(buttonAdd, Priority.NEVER)
  GridPane.setVgrow(buttonAdd, Priority.NEVER)
  add(buttonAdd, 1, 1)

  def loadFiles(files: List[File]): Unit = {
    listFiles.getItems.clear()
    files.foreach { file =>
      listFiles.getItems.add(file.getName.replaceFirst(".json", ""))
    }
  }

}

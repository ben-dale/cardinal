package uk.co.ridentbyte.view.file

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.geometry.{HPos, Insets}
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.control.{Button, ListView, TreeItem, TreeView}
import javafx.scene.layout._

class FilePane(loadFileCallback: (String) => Unit,
               deleteFileCallback: (String) => Unit,
               duplicateFileCallback: (String) => Unit,
               renameFileCallback: (String, String) => Unit) extends GridPane {

  setHgap(5)
  setVgap(5)
  setPadding(new Insets(10, 10, 10, 10))

  private val treeFiles = new TreeView[String]()
  GridPane.setHgrow(treeFiles, Priority.ALWAYS)
  GridPane.setVgrow(treeFiles, Priority.ALWAYS)
//  GridPane.setColumnSpan(treeFiles, 2)
  treeFiles.getSelectionModel.selectedItemProperty().addListener(new ChangeListener[TreeItem[String]] {
    override def changed(observable: ObservableValue[_ <: TreeItem[String]], oldValue: TreeItem[String], newValue: TreeItem[String]): Unit = {
      if (newValue != null) {
        loadFileCallback(newValue.getValue)
      }
    }
  })
  add(treeFiles, 0, 0)
//
//  private val buttonDelete = new Button("Delete")
//  buttonDelete.setStyle("-fx-padding: 2 6 2 6;")
//  buttonDelete.setOnAction((_) => {
//    val selectedIndex = listFiles.getSelectionModel.getSelectedIndex
//    if (selectedIndex >= 0) {
//      deleteFileCallback(listFiles.getSelectionModel.getSelectedItem)
//    }
//  })
//  GridPane.setHgrow(buttonDelete, Priority.ALWAYS)
//  GridPane.setVgrow(buttonDelete, Priority.NEVER)
//  GridPane.setHalignment(buttonDelete, HPos.RIGHT)
//  add(buttonDelete, 0, 1)
//
//  private val buttonDuplicate = new Button("Duplicate")
//  buttonDuplicate.setStyle("-fx-padding: 2 6 2 6;")
//  buttonDuplicate.setOnAction((_) => {
//    val selectedIndex = listFiles.getSelectionModel.getSelectedIndex
//    if (selectedIndex >= 0) {
//      duplicateFileCallback(listFiles.getSelectionModel.getSelectedItem)
//    }
//  })
//  GridPane.setHgrow(buttonDuplicate, Priority.NEVER)
//  GridPane.setVgrow(buttonDuplicate, Priority.NEVER)
//  add(buttonDuplicate, 1, 1)

  def setListContentTo(files: List[String]): Unit = {
    val rootItem = new TreeItem[String]("Cardinal")
    rootItem.setExpanded(true)
    files.sorted.foreach { file =>
      rootItem.getChildren.add(new TreeItem[String](file.replaceFirst(".json", "")))
    }
    treeFiles.setRoot(rootItem)
  }

}

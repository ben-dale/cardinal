package uk.co.ridentbyte.view.file

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.geometry.Insets
import javafx.scene.control._
import javafx.scene.layout._

import scala.collection.JavaConverters._

class FilePane(loadFileCallback: (String) => Unit,
               deleteFileCallback: (String) => Unit,
               duplicateFileCallback: (String) => Unit) extends GridPane {

  setPadding(new Insets(10, 10, 10, 10))

  private val treeFiles = new TreeView[String]()
  treeFiles.setShowRoot(false)
  GridPane.setHgrow(treeFiles, Priority.ALWAYS)
  GridPane.setVgrow(treeFiles, Priority.ALWAYS)
  treeFiles.getSelectionModel.selectedItemProperty().addListener(new ChangeListener[TreeItem[String]] {
    override def changed(observable: ObservableValue[_ <: TreeItem[String]], oldValue: TreeItem[String], newValue: TreeItem[String]): Unit = {
      if (newValue != null && newValue != treeFiles.getRoot) {
        loadFileCallback(newValue.getValue)
      }
    }
  })

  private val menuItemClone = new MenuItem("Clone")
  menuItemClone.setOnAction((_) => {
    val selectedIndex = treeFiles.getSelectionModel.getSelectedIndex
    if (selectedIndex >= 0) {
      duplicateFileCallback(treeFiles.getSelectionModel.getSelectedItem.getValue)
    }
  })

  private val menuItemDelete = new MenuItem("Delete")
  menuItemDelete.setOnAction((_) => {
    val selectedIndex = treeFiles.getSelectionModel.getSelectedIndex
    if (selectedIndex >= 0) {
      deleteFileCallback(treeFiles.getSelectionModel.getSelectedItem.getValue)
    }
  })

  treeFiles.setContextMenu(new ContextMenu(menuItemClone, menuItemDelete))
  add(treeFiles, 0, 0)

  def setListContentTo(files: List[String]): Unit = {
    val rootItem = new TreeItem[String]()
    rootItem.setExpanded(true)
    files.sorted.foreach { file =>
      rootItem.getChildren.add(new TreeItem[String](file.replaceFirst(".json", "")))
    }
    treeFiles.setRoot(rootItem)
  }

  def highlight(item: String): Unit = {
    treeFiles.getRoot.getChildren.asScala.zipWithIndex.foreach { case (c, i) =>
      if (c.getValue == item) {
        treeFiles.getSelectionModel.select(i)
      }
    }
  }

}

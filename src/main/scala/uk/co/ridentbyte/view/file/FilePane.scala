package uk.co.ridentbyte.view.file

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.geometry.Insets
import javafx.scene.control._
import javafx.scene.layout._

import scala.annotation.tailrec
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
      if (newValue != null && newValue != treeFiles.getRoot && newValue.getChildren.isEmpty) {
        loadFileCallback(getFullPathFor(newValue))
      }
    }
  })

  private val menuItemClone = new MenuItem("Clone")
  menuItemClone.setOnAction((_) => {
    val selectedIndex = treeFiles.getSelectionModel.getSelectedIndex
    if (selectedIndex >= 0) {
      duplicateFileCallback(getFullPathFor(treeFiles.getSelectionModel.getSelectedItem))
    }
  })

  private val menuItemDelete = new MenuItem("Delete")
  menuItemDelete.setOnAction((_) => {
    val selectedIndex = treeFiles.getSelectionModel.getSelectedIndex
    if (selectedIndex >= 0) {
      deleteFileCallback(getFullPathFor(treeFiles.getSelectionModel.getSelectedItem))
    }
  })

  treeFiles.setContextMenu(new ContextMenu(menuItemClone, menuItemDelete))
  add(treeFiles, 0, 0)

  def highlight(item: String): Unit = {
    treeFiles.getRoot.getChildren.asScala.zipWithIndex.foreach { case (c, i) =>
      if (c.getValue == item) {
        treeFiles.getSelectionModel.select(i)
      }
    }
  }

  def setListContentTo(files: List[String]): Unit = {
    val rootItem = new TreeItem[String]()
    files.foreach { file =>
      processPath(file, rootItem)
    }
    treeFiles.setRoot(rootItem)
  }

  @tailrec
  private def processPath(path: String, parent: TreeItem[String]): Unit = {
    val tail = path.split("/")
    if (tail.length == 1) {
      parent.getChildren.add(new TreeItem(tail(0).replace(".json", "")))
    } else {
      val existingMatchingParentChildren = parent.getChildren.asScala.filter(!_.getChildren.isEmpty)
      existingMatchingParentChildren.find(_.getValue == tail(0)) match {
        case Some(child) =>
          processPath(tail.tail.mkString("/"), child)
        case _ =>
          val newParent = new TreeItem(tail(0))
          parent.getChildren.add(newParent)
          processPath(tail.tail.mkString("/"), newParent)
      }
    }
  }

  @tailrec
  private def getFullPathFor(treeItem: TreeItem[String], acc: List[String] = List()): String = {
    if (treeItem.getParent == null) {
      acc.mkString("/")
    } else {
      getFullPathFor(treeItem.getParent, treeItem.getValue +: acc)
    }
  }

}

package uk.co.ridentbyte

import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.layout._
import javafx.scene.paint.Color
import javafx.stage.Stage

import uk.co.ridentbyte.model.Header
import uk.co.ridentbyte.view.util.GridConstraints
import uk.co.ridentbyte.view.{RequestPane, ResponsePane}

object Cardinal {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Cardinal], args: _*)
  }
}

class Cardinal extends Application {

  val rootGridPane = new GridPane()
  rootGridPane.getColumnConstraints.add(GridConstraints.widthColumnConstraint(30))
  rootGridPane.getColumnConstraints.add(GridConstraints.widthColumnConstraint(70))
  rootGridPane.getRowConstraints.add(GridConstraints.maxHeightRowConstraint)

  val requestPane = new RequestPane(loadResponse, showErrorDialog)
  val responsePane = new ResponsePane()

  override def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("Cardinal")

    rootGridPane.add(requestPane, 0, 0)
    rootGridPane.add(responsePane, 1, 0)

    val scene = new Scene(rootGridPane, 800, 400)
    primaryStage.setScene(scene)
    primaryStage.show()
  }

  def loadResponse(headers: Iterable[Header], body: String): Unit = {
    responsePane.loadResponse(headers, body)
  }

  def showErrorDialog(errorMessage: String): Unit = {
    val alert = new Alert(AlertType.ERROR)
    alert.setContentText(errorMessage)
    alert.showAndWait
  }

}
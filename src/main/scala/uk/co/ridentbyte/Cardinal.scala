package uk.co.ridentbyte

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.layout.GridPane
import javafx.stage.Stage

import uk.co.ridentbyte.model.Header
import uk.co.ridentbyte.view.RequestPane

object Cardinal {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Cardinal], args: _*)
  }
}

class Cardinal extends Application {

  val rootGridPane = new GridPane()

  val requestPane = new RequestPane(loadResponse, showErrorDialog)

  val responseTabPane = new TabPane()

  val responseBodyTextArea = new TextArea()
  responseBodyTextArea.setEditable(false)

  val responseHeadersListView = new ListView[String]()

  override def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("Cardinal")
    primaryStage.setWidth(750)
    primaryStage.setHeight(500)
    primaryStage.setMinHeight(500)
    primaryStage.setMinWidth(500)


    val bodyTab = new Tab("Body")
    bodyTab.setClosable(false)
    bodyTab.setContent(responseBodyTextArea)
    responseTabPane.getTabs.add(bodyTab)

    val headersTab = new Tab("Headers")
    headersTab.setClosable(false)
    headersTab.setContent(responseHeadersListView)
    responseTabPane.getTabs.add(headersTab)

    rootGridPane.add(requestPane, 0, 0)
    rootGridPane.add(responseTabPane, 1, 0)

    val scene = new Scene(rootGridPane)
    primaryStage.setScene(scene)

    primaryStage.show()
  }

  def loadResponse(headers: Iterable[Header], body: String): Unit = {
    responseHeadersListView.getItems.clear()
    headers.foreach { header => responseHeadersListView.getItems.add(header.toString) }
    responseBodyTextArea.setText(body)
  }

  def showErrorDialog(errorMessage: String): Unit = {
    val alert = new Alert(AlertType.ERROR)
    alert.setContentText(errorMessage)
    alert.showAndWait
  }



}
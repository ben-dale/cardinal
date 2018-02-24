package uk.co.ridentbyte

import java.io.{File, PrintWriter}
import java.net.{ConnectException, URISyntaxException, UnknownHostException}
import java.nio.file.{Files, Paths}
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.layout._
import javafx.stage.Stage
import javax.net.ssl.SSLHandshakeException

import uk.co.ridentbyte.model.{HttpResponseWrapper, Request}
import uk.co.ridentbyte.util.HttpUtil
import uk.co.ridentbyte.view.file.FilePane
import uk.co.ridentbyte.view.util.GridConstraints
import uk.co.ridentbyte.view.request.RequestPane
import uk.co.ridentbyte.view.response.ResponsePane

object Cardinal {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Cardinal], args: _*)
  }
}

class Cardinal extends Application {

  val fileDir = "cardinal_files"

  val rootGridPane = new GridPane()
  rootGridPane.getColumnConstraints.add(GridConstraints.widthColumnConstraint(20))
  rootGridPane.getColumnConstraints.add(GridConstraints.widthColumnConstraint(35))
  rootGridPane.getColumnConstraints.add(GridConstraints.widthColumnConstraint(45))
  rootGridPane.getRowConstraints.add(GridConstraints.maxHeightRowConstraint)

  val filePane = new FilePane
  val requestPane = new RequestPane(sendRequest, clearAll)
  val responsePane = new ResponsePane()

  override def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("Cardinal")
    primaryStage.setMinHeight(400)
    primaryStage.setMinWidth(800)

    rootGridPane.add(filePane, 0, 0)
    rootGridPane.add(requestPane, 1, 0)
    rootGridPane.add(responsePane, 2, 0)

    val scene = new Scene(rootGridPane, 1000, 500)
    primaryStage.setScene(scene)
    primaryStage.show()

    filePane.loadFiles(loadFiles())
  }

  def showErrorDialog(errorMessage: String): Unit = {
    val alert = new Alert(AlertType.ERROR)
    alert.setContentText(errorMessage)
    alert.showAndWait
  }

  def sendRequest(request: Request): Unit = {
    try {
      val startTime = System.currentTimeMillis()
      val response = HttpUtil.sendRequest(request)
      val totalTime = System.currentTimeMillis() - startTime
      val httpResponse = HttpResponseWrapper(response, totalTime)
      responsePane.loadResponse(httpResponse)
    } catch {
      case _: ConnectException => showErrorDialog("Connection refused")
      case _: URISyntaxException => showErrorDialog("Invalid URL")
      case _: UnknownHostException => showErrorDialog("Unknown Host")
      case _: SSLHandshakeException => showErrorDialog("SSL Handshake failed. Remote host closed connection during handshake.")
    }
  }

  def clearAll(): Unit = {
    requestPane.clear()
    responsePane.clear()
  }

  def loadFiles(): List[File] = {
    val d = new File(fileDir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      Files.createDirectories(Paths.get(fileDir))
      List.empty[File]
    }
  }

  def saveFile(): Unit = {
    val d = new File(fileDir)
    if (!d.exists || !d.isDirectory) {
      Files.createDirectories(Paths.get(fileDir))
    }

    val pw = new PrintWriter(fileDir + "/" + System.currentTimeMillis() + ".json")
    pw.write("Hello, world")
    pw.close()
  }

}
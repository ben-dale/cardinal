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
import uk.co.ridentbyte.view.request.{RequestControlPane, RequestInputPane}
import uk.co.ridentbyte.view.response.ResponsePane

import scala.io.Source

object Cardinal {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Cardinal], args: _*)
  }
}

class Cardinal extends Application {

  val fileDir = "cardinal_files"

  val rootGridPane = new GridPane()
  rootGridPane.getColumnConstraints.add(GridConstraints.widthColumnConstraint(25))
  rootGridPane.getColumnConstraints.add(GridConstraints.widthColumnConstraint(75))
  rootGridPane.getRowConstraints.addAll(GridConstraints.maxHeightRowConstraint)

  val filePane = new FilePane(loadFile)
  val requestInputPane = new RequestInputPane
  val responsePane = new ResponsePane()
  val requestControlPane = new RequestControlPane(sendRequest, clearAll, save)

  override def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("Cardinal")
    primaryStage.setMinHeight(400)
    primaryStage.setMinWidth(800)

    val grid2 = new GridPane
    grid2.getColumnConstraints.add(GridConstraints.widthColumnConstraint(40))
    grid2.getColumnConstraints.add(GridConstraints.widthColumnConstraint(60))
    grid2.getRowConstraints.add(GridConstraints.maxHeightRowConstraint)
    grid2.getRowConstraints.add(GridConstraints.noGrowRowConstraint)

    grid2.add(requestInputPane, 0, 0)
    grid2.add(responsePane, 1, 0)

    GridPane.setColumnSpan(requestControlPane, 2)
    grid2.add(requestControlPane, 0, 1)

    rootGridPane.add(filePane, 0, 0)
    rootGridPane.add(grid2, 1, 0)

    val scene = new Scene(rootGridPane, 1000, 500)
    primaryStage.setScene(scene)
    primaryStage.show()

    filePane.loadFiles(loadFiles())
  }

  def sendRequest(): Unit = {
    sendRequest(requestInputPane.getRequest)
  }

  def save(): Unit = {
    save(requestInputPane.getRequest)
  }

  def showErrorDialog(errorMessage: String): Unit = {
    val alert = new Alert(AlertType.ERROR)
    alert.setContentText(errorMessage)
    alert.showAndWait
  }

  def showInputDialog: Option[String] = {
    val alert = new TextInputDialog()
    alert.setContentText("Please enter filename")
    val result = alert.showAndWait()
    if (result.isPresent) {
      Some(result.get)
    } else {
      None
    }
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
    requestInputPane.clear()
    responsePane.clear()
  }


  def save(request: Request): Unit = {
    if (request.name.isDefined) {
      saveFile(request.name.get + ".json", request.toJson)
      filePane.loadFiles(loadFiles())
    } else {
      val result = showInputDialog
      if (result.isDefined) {
        saveFile(result.get + ".json", request.withName(result.get).toJson)
        filePane.loadFiles(loadFiles())
      }
    }
  }

  def loadFile(filename: String): Unit = {
    try {
      clearAll()
      val bufferedSource = Source.fromFile(fileDir + "/" + filename)
      val rawRequest = bufferedSource.getLines.mkString
      bufferedSource.close()
      loadRequest(Request(rawRequest))
    } catch {
      case _: Exception =>
        filePane.loadFiles(loadFiles())
        showErrorDialog("Error loading: " + filename)
    }
  }

  private def loadRequest(request: Request): Unit = {
    clearAll()
    requestInputPane.loadRequest(request)
  }

  private def loadFiles(): List[File] = {
    val d = new File(fileDir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      Files.createDirectories(Paths.get(fileDir))
      List.empty[File]
    }
  }

  private def saveFile(filename: String, data: String): Unit = {
    val d = new File(fileDir)
    if (!d.exists || !d.isDirectory) {
      Files.createDirectories(Paths.get(fileDir))
    }

    val pw = new PrintWriter(fileDir + "/" + filename)
    pw.write(data)
    pw.close()
  }

}
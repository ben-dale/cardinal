package uk.co.ridentbyte

import java.net.{ConnectException, URISyntaxException, UnknownHostException}
import javafx.application.{Application, Platform}
import javafx.scene.Scene
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.layout.{GridPane, Priority}
import javafx.stage.Stage
import javax.net.ssl.SSLHandshakeException

import uk.co.ridentbyte.model.{HttpResponseWrapper, Request}
import uk.co.ridentbyte.util.{HttpUtil, IOUtil}
import uk.co.ridentbyte.view.file.FilePane
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}
import uk.co.ridentbyte.view.request.{BulkRequestInputDialog, BulkRequestProcessingDialog, RequestControlPane, RequestInputPane}
import uk.co.ridentbyte.view.response.ResponsePane

object Cardinal {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Cardinal], args: _*)
  }
}

class Cardinal extends Application {

  private val fileDir = "cardinal_files"

  private val rootGridPane = new GridPane()
  rootGridPane.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(25).build)
  rootGridPane.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(75).build)
  rootGridPane.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build)

  private val filePane = new FilePane(loadFile, deleteFile, duplicateFile, renameFile)
  private val requestInputPane = new RequestInputPane
  private val responsePane = new ResponsePane()
  private val requestControlPane = new RequestControlPane(sendRequestAndLoadResponse, showBulkRequestDialog, clearInputOutputPanes, save)

  override def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("Cardinal")
    primaryStage.setMinHeight(400)
    primaryStage.setMinWidth(800)

    val grid2 = new GridPane
    grid2.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(40).build)
    grid2.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(60).build)
    grid2.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build)
    grid2.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.NEVER).build)

    grid2.add(requestInputPane, 0, 0)
    grid2.add(responsePane, 1, 0)

    GridPane.setColumnSpan(requestControlPane, 2)
    grid2.add(requestControlPane, 0, 1)

    rootGridPane.add(filePane, 0, 0)
    rootGridPane.add(grid2, 1, 0)

    val scene = new Scene(rootGridPane, 1000, 500)
    primaryStage.setScene(scene)
    primaryStage.show()

    filePane.setListContentTo(IOUtil.listFileNames(fileDir))
  }


  private def save(): Unit = {
    save(requestInputPane.getRequest)
  }

  private def showErrorDialog(errorMessage: String): Unit = {
    val alert = new Alert(AlertType.ERROR)
    alert.setContentText(errorMessage)
    alert.showAndWait
  }

  private def showInputDialog: Option[String] = {
    val alert = new TextInputDialog()
    alert.setContentText("Please enter filename")
    val result = alert.showAndWait()
    if (result.isPresent) {
      Some(result.get)
    } else {
      None
    }
  }

  private def sendRequest(request: Request): HttpResponseWrapper = {
    val startTime = System.currentTimeMillis()
    val response = HttpUtil.sendRequest(request)
    val totalTime = System.currentTimeMillis() - startTime
    HttpResponseWrapper(response, totalTime)
  }

  private def sendRequestAndLoadResponse(): Unit = {
    try {
      val httpResponse = sendRequest(requestInputPane.getRequest)
      responsePane.loadResponse(httpResponse)
    } catch {
      case _: ConnectException => showErrorDialog("Connection refused.")
      case _: URISyntaxException => showErrorDialog("Invalid URL.")
      case _: UnknownHostException => showErrorDialog("Unknown Host.")
      case _: SSLHandshakeException => showErrorDialog("SSL Handshake failed. Remote host closed connection during handshake.")
      case _: Exception => showErrorDialog("Unknown error occurred.")
    }
  }

  private def showBulkRequestResultDialog(responses: List[Option[HttpResponseWrapper]]): Unit = {
    Platform.runLater(() => {
      val alert = new Alert(AlertType.INFORMATION)
      alert.setTitle("Bulk Request Complete")
      alert.setHeaderText("Bulk Request Complete")
      val completeRequests = responses.filter(_.isDefined)
      val failedRequests = responses.filter(_.isEmpty)
      alert.setContentText(s"Completed ${completeRequests.size} requests\nFailed ${failedRequests.size} requests")
      alert.show()
    })
  }

  private def showBulkRequestDialog(): Unit = {
    val request = requestInputPane.getRequest
    val dialog = new BulkRequestInputDialog
    val results = dialog.showAndWait()
    if (results.isPresent) {
      val throttle = if (results.get._1.trim.length > 0) Some(results.get._1.trim.toLong) else None
      val count = if (results.get._2.trim.length > 0) Some(results.get._2.trim.toInt) else None
      val ids = results.get._3.split(",").map(_.trim).toList
      new BulkRequestProcessingDialog(count, throttle, ids, request, sendRequest, showBulkRequestResultDialog).show()
    }
  }

  private def clearInputOutputPanes(): Unit = {
    requestInputPane.clear()
    responsePane.clear()
  }

  private def save(request: Request): Unit = {
    if (request.name.isDefined) {
      IOUtil.writeToFile(fileDir, request.name.get + ".json", request.toJson)
      filePane.setListContentTo(IOUtil.listFileNames(fileDir))
    } else {
      val result = showInputDialog
      if (result.isDefined) {
        IOUtil.writeToFile(fileDir, result.get + ".json", request.withName(result.get).toJson)
        filePane.setListContentTo(IOUtil.listFileNames(fileDir))
      }
    }
  }

  private def loadRequest(request: Request): Unit = {
    clearInputOutputPanes()
    requestInputPane.loadRequest(request)
  }

  private def loadFile(filename: String): Unit = {
    try {
      clearInputOutputPanes()
      val data = IOUtil.loadFileData(fileDir + "/" + filename + ".json")
      loadRequest(Request(data))
    } catch {
      case _: Exception =>
        filePane.setListContentTo(IOUtil.listFileNames(fileDir))
        showErrorDialog("Error loading: " + filename)
    }
  }

  private def loadFileAsRequest(filename: String): Option[Request] = {
    try {
      clearInputOutputPanes()
      val data = IOUtil.loadFileData(fileDir + "/" + filename + ".json")
      Some(Request(data))
    } catch {
      case _: Exception =>
        filePane.setListContentTo(IOUtil.listFileNames(fileDir))
        showErrorDialog("Error loading: " + filename)
        None
    }
  }

  private def duplicateFile(filename: String): Unit = {
    val request = loadFileAsRequest(filename)
    if (request.isDefined) {
      val newName = showInputDialog
      if (newName.isDefined) {
        save(request.get.withName(newName.get))
        filePane.setListContentTo(IOUtil.listFileNames(fileDir))
      }
    }
  }

  private def renameFile(filename: String, newFilename: String): Unit = {
    val original = loadFileAsRequest(filename)
    if (original.isDefined) {
      save(original.get.withName(newFilename))
      deleteFile(filename)
      filePane.setListContentTo(IOUtil.listFileNames(fileDir))
    }
  }

  private def deleteFile(filename: String): Unit = {
    IOUtil.deleteFile(fileDir + "/" + filename + ".json")
    filePane.setListContentTo(IOUtil.listFileNames(fileDir))
    clearInputOutputPanes()
  }
}
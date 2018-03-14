package uk.co.ridentbyte

import java.io.File
import java.net.{ConnectException, URISyntaxException, UnknownHostException}
import javafx.application.{Application, Platform}
import javafx.scene.Scene
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.layout.{GridPane, Priority}
import javafx.stage.Stage
import javax.net.ssl.SSLHandshakeException

import uk.co.ridentbyte.model.{BulkRequest, HttpResponseWrapper, Request}
import uk.co.ridentbyte.util.{HttpUtil, IOUtil}
import uk.co.ridentbyte.view.file.FilePane
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}
import uk.co.ridentbyte.view.request.{BulkRequestInputDialog, BulkRequestProcessingDialog, RequestControlPane, RequestInputPane}
import uk.co.ridentbyte.view.response.ResponsePane

import scala.annotation.tailrec

object Cardinal {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Cardinal], args: _*)
  }
}

class Cardinal extends Application {

  private var currentFile: Option[File] = None

  private val fileDir = "cardinal_files"

  private val rootGridPane = new GridPane()
  rootGridPane.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(25).build)
  rootGridPane.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(75).build)
  rootGridPane.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build)

  private val filePane = new FilePane(loadFile, deleteFile, duplicateFile)
  private val requestInputPane = new RequestInputPane
  private val responsePane = new ResponsePane()
  private val requestControlPane = new RequestControlPane(sendRequestAndLoadResponse, showBulkRequestDialogNoArgs, clearAll, save)

  override def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("Cardinal")
    primaryStage.setMinHeight(400)
    primaryStage.setMinWidth(800)

    val grid2 = new GridPane
    grid2.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(45).build)
    grid2.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(55).build)
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
      val httpResponse = sendRequest(requestInputPane.getRequest.processConstants())
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

  private def showBulkRequestDialogNoArgs(): Unit = {
    showBulkRequestDialog()
  }

  @tailrec
  private def showBulkRequestDialog(bulkRequest: BulkRequest = BulkRequest()): Unit = {
    val request = requestInputPane.getRequest
    val dialog = new BulkRequestInputDialog(bulkRequest)
    val results = dialog.showAndWait()
    if (results.isPresent) {
      val throttle = results.get.throttle
      val count = results.get.count
      val ids = results.get.ids

      if (count.isEmpty && ids.isEmpty) {
        showErrorDialog("Enter a for each value or count value.")
        showBulkRequestDialog(BulkRequest(throttle, count, ids))
      } else if (count.isDefined && ids.nonEmpty) {
        showErrorDialog("Enter only a for each value or count value.")
        showBulkRequestDialog(BulkRequest(throttle, count, ids))
      } else {
        new BulkRequestProcessingDialog(count, throttle, ids, request, sendRequest, showBulkRequestResultDialog).show()
      }
    }
  }

  private def clearAll(): Unit = {
    currentFile = None
    requestInputPane.clear()
    responsePane.clear()
  }

  private def save(): Unit = {
    save(requestInputPane.getRequest)
  }

  private def save(request: Request, filename: Option[String] = None): Unit = {
    if (filename.isDefined) {
      IOUtil.writeToFile(fileDir + "/" + filename.get + ".json", request.toJson)
      filePane.setListContentTo(IOUtil.listFileNames(fileDir))
    } else if (currentFile.isDefined) {
      IOUtil.writeToFile(fileDir + "/" + currentFile.get.getName, request.toJson)
      filePane.setListContentTo(IOUtil.listFileNames(fileDir))
    } else {
      val result = showInputDialog
      if (result.isDefined) {
        IOUtil.writeToFile(fileDir + "/" + result.get + ".json", request.toJson)
        filePane.setListContentTo(IOUtil.listFileNames(fileDir))
      }
    }
  }

  private def loadRequest(request: Request): Unit = {
    requestInputPane.loadRequest(request)
  }

  private def loadFile(filename: String): Unit = {
    try {
      clearAll()
      currentFile = Some(IOUtil.loadFile(fileDir + "/" + filename + ".json"))
      println(currentFile.get.getName)
      val data = IOUtil.readFileContents(currentFile.get)
      loadRequest(Request(data))
    } catch {
      case _: Exception =>
        filePane.setListContentTo(IOUtil.listFileNames(fileDir))
        showErrorDialog("Error loading: " + filename)
    }
  }

  private def loadFileAsRequest(filename: String): Option[Request] = {
    try {
      clearAll()
      currentFile = Some(IOUtil.loadFile(fileDir + "/" + filename + ".json"))
      val data = IOUtil.readFileContents(currentFile.get)
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
        save(request.get, Some(newName.get))
        currentFile = Some(IOUtil.loadFile(newName.get))
        filePane.setListContentTo(IOUtil.listFileNames(fileDir))
        filePane.highlight(newName.get)
      }
    }
  }

  private def deleteFile(filename: String): Unit = {
    IOUtil.deleteFile(fileDir + "/" + filename + ".json")
    filePane.setListContentTo(IOUtil.listFileNames(fileDir))
    clearAll()
  }
}
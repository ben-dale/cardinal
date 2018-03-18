package uk.co.ridentbyte.view

import java.net.{ConnectException, URISyntaxException, UnknownHostException}

import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, TextInputDialog}
import javafx.scene.layout.{GridPane, Priority}
import javax.net.ssl.SSLHandshakeException
import uk.co.ridentbyte.model.{HttpResponseWrapper, Request}
import uk.co.ridentbyte.view.file.FilePane
import uk.co.ridentbyte.view.request.{RequestControlPane, RequestInputPane}
import uk.co.ridentbyte.view.response.ResponsePane
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}

class CardinalView(loadFile: (String) => Unit,
                   deleteFile: (String) => Unit,
                   clearAll: () => Unit,
                   saveCallback: (Request, Option[String]) => Unit,
                   duplicateFile: (String) => Unit,
                   sendRequest: (Request) => HttpResponseWrapper) extends GridPane {

  getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(25).build)
  getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(75).build)
  getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build)

  private val filePane = new FilePane(loadFile, deleteFile, duplicateFile)
  private val requestInputPane = new RequestInputPane
  private val responsePane = new ResponsePane(sendRequest)
  private val requestControlPane = new RequestControlPane(sendRequestAndLoadResponse, showBulkRequestDialogNoArgs, clearAll, save)

  val grid2 = new GridPane
  grid2.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(45).build)
  grid2.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(55).build)
  grid2.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build)
  grid2.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.NEVER).build)

  grid2.add(requestInputPane, 0, 0)
  grid2.add(responsePane, 1, 0)

  GridPane.setColumnSpan(requestControlPane, 2)
  grid2.add(requestControlPane, 0, 1)

  add(filePane, 0, 0)
  add(grid2, 1, 0)


  def setFileList(fileNames: List[String]): Unit = {
    filePane.setListContentTo(fileNames)
  }

  def showInputDialog(defaultValue: String = ""): Option[String] = {
    val alert = new TextInputDialog(defaultValue)
    alert.setContentText("Please enter filename")
    val result = alert.showAndWait()
    if (result.isPresent) {
      Some(result.get)
    } else {
      None
    }
  }

  def showErrorDialog(errorMessage: String): Unit = {
    val alert = new Alert(AlertType.ERROR)
    alert.setContentText(errorMessage)
    alert.showAndWait
  }

  def clearRequestResponsePanes(): Unit = {
    requestInputPane.clear()
    responsePane.clear()
  }

  private def sendRequestAndLoadResponse(): Unit = {
    val request = requestInputPane.getRequest
    if (request.uri.trim.length == 0) {
      showErrorDialog("Please enter a URL.")
    } else {
      try {
        val httpResponse = sendRequest(requestInputPane.getRequest.processConstants())
        responsePane.addResponse(httpResponse)
      } catch {
        case _: ConnectException => showErrorDialog("Connection refused.")
        case _: URISyntaxException => showErrorDialog("Invalid URL.")
        case _: UnknownHostException => showErrorDialog("Unknown Host.")
        case _: SSLHandshakeException => showErrorDialog("SSL Handshake failed. Remote host closed connection during handshake.")
        case _: Exception => showErrorDialog("Unknown error occurred.")
      }
    }
  }

  private def showBulkRequestDialogNoArgs(): Unit = {
    responsePane.showBulkRequestInput(requestInputPane.getRequest)
  }

  def loadRequest(request: Request): Unit = {
    requestInputPane.loadRequest(request)
  }

  def highlightFile(filename: String): Unit = {
    filePane.highlight(filename)
  }

  private def save(): Unit = {
    saveCallback(requestInputPane.getRequest, None)
  }

}

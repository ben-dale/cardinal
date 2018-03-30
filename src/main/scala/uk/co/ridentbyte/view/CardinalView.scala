package uk.co.ridentbyte.view

import java.net.{ConnectException, URISyntaxException, UnknownHostException}
import java.nio.charset.StandardCharsets
import java.util.Base64

import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.layout.{BorderPane, GridPane, Priority}
import javax.net.ssl.SSLHandshakeException
import uk.co.ridentbyte.model.{HttpResponseWrapper, Request}
import uk.co.ridentbyte.view.file.FilePane
import uk.co.ridentbyte.view.request.RequestInputPane
import uk.co.ridentbyte.view.response.ResponsePane
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}
import uk.co.ridentbyte.view.dialog.BasicAuthInputDialog

class CardinalView(loadFileCallback: (String) => Unit,
                   deleteFile: (String) => Unit,
                   clearAllCallback: () => Unit,
                   saveCallback: (Request, Option[String]) => Unit,
                   duplicateFileCallback: (String) => Unit,
                   sendRequestCallback: (Request) => HttpResponseWrapper) extends BorderPane {

  private val filePane = new FilePane(loadFileCallback, deleteFile, duplicateFileCallback)
  private val requestInputPane = new RequestInputPane(sendRequestAndLoadResponse, showBulkRequest)
  private val responsePane = new ResponsePane(sendRequestCallback)

  val grid = new GridPane
  grid.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(45).build)
  grid.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(55).build)
  grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build)
  grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.NEVER).build)

  grid.add(requestInputPane, 0, 0)
  grid.add(responsePane, 1, 0)

  val grid2 = new GridPane
  grid2.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(25).build)
  grid2.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(75).build)
  grid2.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build)
  grid2.add(filePane, 0, 0)
  grid2.add(grid, 1, 0)

  val menuBar = new MenuBar
  if (System.getProperty("os.name").toLowerCase.contains("mac")) {
    menuBar.setUseSystemMenuBar(true)
  }

  val menuFile = new Menu("File")

  val menuItemSaveAs = new MenuItem("Save As...")
  menuItemSaveAs.setOnAction((_) => save())
  menuFile.getItems.add(menuItemSaveAs)

  val menuItemClearAll = new MenuItem("Clear All")
  menuItemClearAll.setOnAction((_) => clearAllCallback())
  menuFile.getItems.add(menuItemClearAll)

  menuBar.getMenus.add(menuFile)

  val menuTools = new Menu("Tools")

  val menuItemViewAsCurl = new MenuItem("View as cURL")
  menuItemViewAsCurl.setOnAction((_) => {
    val request = getRequest
    if (request.uri.trim.length == 0) {
      showErrorDialog("Please enter a URL.")
    } else {
      responsePane.setCurlCommand(request.toCurl)
    }
  })
  menuTools.getItems.add(menuItemViewAsCurl)

  menuBar.getMenus.add(menuTools)

  val menuAuthorisation = new Menu("Auth")

  val menuItemBasicAuth = new MenuItem("Basic Auth...")
  menuItemBasicAuth.setOnAction((_) => showBasicAuthInput())
  menuAuthorisation.getItems.add(menuItemBasicAuth)

  menuBar.getMenus.add(menuAuthorisation)

  setTop(menuBar)
  setCenter(grid2)

  def setFileList(fileNames: List[String]): Unit = {
    filePane.setListContentTo(fileNames)
  }

  def addFile(fileName: String): Unit = filePane.add(fileName)

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

  def clearRequestPane(): Unit = {
    requestInputPane.clear()
  }

  def clearRequestResponsePanes(): Unit = {
    requestInputPane.clear()
    responsePane.clearContents()
    filePane.removeSelection()
  }

  def getRequest: Request = {
    requestInputPane.getRequest
  }

  private def sendRequestAndLoadResponse(): Unit = {
    val request = requestInputPane.getRequest
    if (request.uri.trim.length == 0) {
      showErrorDialog("Please enter a URL.")
    } else {
      try {
        val httpResponse = sendRequestCallback(requestInputPane.getRequest.processConstants())
        responsePane.setResponse(httpResponse)
      } catch {
        case _: ConnectException => showErrorDialog("Connection refused.")
        case _: URISyntaxException => showErrorDialog("Invalid URL.")
        case _: UnknownHostException => showErrorDialog("Unknown Host.")
        case _: SSLHandshakeException => showErrorDialog("SSL Handshake failed. Remote host closed connection during handshake.")
        case _: Exception => showErrorDialog("Unknown error occurred.")
      }
    }
  }

  private def showBulkRequest(): Unit = {
    val request = requestInputPane.getRequest
    if (request.uri.trim.length == 0) {
      showErrorDialog("Please enter a URL.")
    } else {
      responsePane.showBulkRequestInput(requestInputPane.getRequest)
    }
  }

  def loadRequest(request: Request): Unit = {
    requestInputPane.loadRequest(request)
  }

  def selectFile(filename: String): Unit = {
    filePane.highlight(filename)
  }

  private def save(): Unit = {
    saveCallback(requestInputPane.getRequest, None)
  }

  def showBasicAuthInput(): Unit = {
    val dialog = new BasicAuthInputDialog
    val results = dialog.showAndWait()
    if (results.isPresent) {
      val username = results.get.username
      val password = results.get.password
      val encoded = Base64.getEncoder.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8))
      requestInputPane.addHeader("Authorization: Basic " + encoded)
    }
  }

}

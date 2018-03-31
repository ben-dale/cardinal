package uk.co.ridentbyte.view

import java.io.File
import java.net.{ConnectException, URISyntaxException, UnknownHostException}
import java.nio.charset.StandardCharsets
import java.util.Base64

import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.layout.{BorderPane, GridPane, Priority}
import javafx.stage.FileChooser
import javax.net.ssl.SSLHandshakeException
import uk.co.ridentbyte.model.{HttpResponseWrapper, Request}
import uk.co.ridentbyte.util.IOUtil
import uk.co.ridentbyte.view.request.{RequestControlPane, RequestInputPane}
import uk.co.ridentbyte.view.response.ResponsePane
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}
import uk.co.ridentbyte.view.dialog.{BasicAuthInputDialog, FormUrlEncodedInputDialog}

class CardinalView(clearAllCallback: () => Unit,
                   saveChangesToCurrentFileCallback: (Request) => Unit,
                   setCurrentFileCallback: (File) => Unit,
                   sendRequestCallback: (Request) => HttpResponseWrapper) extends BorderPane {

  private val requestInputPane = new RequestInputPane
  private val responsePane = new ResponsePane(sendRequestCallback, showErrorDialog)
  private val requestControlPane = new RequestControlPane(sendRequestAndLoadResponse, showBulkRequest)

  val grid = new GridPane
  grid.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(45).build)
  grid.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(55).build)
  grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build)
  grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.NEVER).build)

  grid.add(requestInputPane, 0, 0)
  grid.add(responsePane, 1, 0)

  GridPane.setColumnSpan(requestControlPane, 2)
  grid.add(requestControlPane, 0, 1)

  val menuBar = new MenuBar
  if (System.getProperty("os.name").toLowerCase.contains("mac")) {
    menuBar.setUseSystemMenuBar(true)
  }

  val menuFile = new Menu("File")

  val menuItemOpen = new MenuItem("Open...")
  menuItemOpen.setOnAction((_) => open())
  menuFile.getItems.add(menuItemOpen)

  val menuItemSave = new MenuItem("Save")
  menuItemSave.setDisable(true)
  menuItemSave.setOnAction((_) => save())
  menuFile.getItems.add(menuItemSave)

  val menuItemSaveAs = new MenuItem("Save As...")
  menuItemSaveAs.setOnAction((_) => saveAs())
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


  val menuForm = new Menu("Form")

  val menuItemUrlEncoded = new MenuItem("URL Encoded...")
  menuItemUrlEncoded.setOnAction((_) => showFormUrlEncodedInput())
  menuForm.getItems.add(menuItemUrlEncoded)

  menuBar.getMenus.add(menuForm)

  setTop(menuBar)
  setCenter(grid)

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

  def clearAll(): Unit = {
    requestInputPane.clear()
    responsePane.clearContents()
    menuItemSave.setDisable(true)
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
    clearAllCallback()
    requestInputPane.loadRequest(request)
  }

  private def save(): Unit = {
    saveChangesToCurrentFileCallback(requestInputPane.getRequest)
  }

  private def saveAs(): Unit = {
    val fileChooser = new FileChooser
    val file = fileChooser.showSaveDialog(null)
    if (file != null) {
      val fileWithExtension = new File(file.getAbsolutePath + ".json")
      IOUtil.writeToFile(fileWithExtension, requestInputPane.getRequest.toJson)
      setCurrentFileCallback(fileWithExtension)
      menuItemSave.setDisable(false)
    }
  }

  private def open(): Unit = {
    val fileChooser = new FileChooser
    val selectedFile = fileChooser.showOpenDialog(null)
    if (selectedFile != null) {
      val lines = scala.io.Source.fromFile(selectedFile).getLines().mkString
      loadRequest(Request(lines))
      setCurrentFileCallback(selectedFile)
      menuItemSave.setDisable(false)
    }
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

  def showFormUrlEncodedInput(): Unit = {
    val dialog = new FormUrlEncodedInputDialog(requestInputPane.getRequest.body.getOrElse(""))
    val results = dialog.showAndWait()
    if (results.isPresent) {
      requestInputPane.setBody(results.get.toBodyString)
      requestInputPane.addHeader("Content-Type: application/x-www-form-urlencoded")
    }
  }



}

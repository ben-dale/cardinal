package uk.co.ridentbyte.view

import java.net.{ConnectException, URISyntaxException, UnknownHostException}

import javafx.application.Platform
import javafx.scene.layout.{BorderPane, GridPane, Priority}
import javax.net.ssl.SSLHandshakeException
import uk.co.ridentbyte.model.{Config, HttpResponseWrapper, Request}
import uk.co.ridentbyte.view.request.{RequestControlPane, RequestInputPane}
import uk.co.ridentbyte.view.response.ResponsePane
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}

class CardinalView(showErrorDialogCallback:(String) => Unit,
                   getConfigCallback: () => Config,
                   sendRequestCallback: (Request) => HttpResponseWrapper,
                   triggerUnsavedChangesMade: () => Unit) extends BorderPane {

  private val requestInputPane = new RequestInputPane(triggerUnsavedChangesMade)
  private val responsePane = new ResponsePane(getConfigCallback, sendRequestCallback, showErrorDialogCallback)
  private val requestControlPane = new RequestControlPane(sendRequestAndLoadResponse, showBulkRequestInput)

  val grid = new GridPane
  grid.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(45).build)
  grid.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).withPercentageWidth(55).build)
  grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build)
  grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.NEVER).build)

  grid.add(requestInputPane, 0, 0)
  grid.add(responsePane, 1, 0)

  GridPane.setColumnSpan(requestControlPane, 2)
  grid.add(requestControlPane, 0, 1)

  setCenter(grid)

  def clearAll(): Unit = {
    requestInputPane.clear()
    responsePane.clearContents()
  }

  private def sendRequestAndLoadResponse(onStart: () => Unit, onFinish: () => Unit): Unit = {
    val request = requestInputPane.getRequest
    if (request.uri.trim.length == 0) {
      showErrorDialogCallback("Please enter a URL.")
    } else {
      new Thread() {
        override def run(): Unit = {
          Platform.runLater(() => onStart())
          val httpResponse = try {
            Thread.sleep(2000)
            Some(sendRequestCallback(requestInputPane.getRequest.processConstants(getConfigCallback())))
          } catch {
            case _: ConnectException => onFinish(); showErrorDialogCallback("Connection refused."); None
            case _: URISyntaxException => onFinish(); showErrorDialogCallback("Invalid URL."); None
            case _: UnknownHostException => onFinish(); showErrorDialogCallback("Unknown Host."); None
            case _: SSLHandshakeException => onFinish(); showErrorDialogCallback("SSL Handshake failed. Remote host closed connection during handshake."); None
            case _: Exception => onFinish(); showErrorDialogCallback("Unknown error occurred."); None
          }
          Platform.runLater(() => {
            responsePane.setResponse(httpResponse)
            onFinish()
          })
        }
      }.start()
    }
  }

  private def showBulkRequestInput(): Unit = {
    val request = requestInputPane.getRequest
    if (request.uri.trim.length == 0) {
      showErrorDialogCallback("Please enter a URL.")
    } else {
      responsePane.showBulkRequestInput(requestInputPane.getRequest)
    }
  }

  def loadRequest(request: Request): Unit = requestInputPane.loadRequest(request)

  def loadCurlCommand(curl: String): Unit = responsePane.loadCurlCommand(curl)

  def getRequest: Request = requestInputPane.getRequest

  def setBody(body: String): Unit = requestInputPane.setBody(Some(body))

  def addHeader(header: String): Unit = requestInputPane.addHeader(header)

}

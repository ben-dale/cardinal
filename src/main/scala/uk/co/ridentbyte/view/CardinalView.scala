package uk.co.ridentbyte.view

import java.net.{ConnectException, URISyntaxException, UnknownHostException}

import javafx.application.Platform
import javafx.scene.control.SplitPane
import javafx.scene.layout.{BorderPane, GridPane, Priority}
import javax.net.ssl.SSLHandshakeException
import uk.co.ridentbyte.model.{CardinalRequest, CardinalResponse, Config}
import uk.co.ridentbyte.view.request.{RequestControlPane, RequestInputPane}
import uk.co.ridentbyte.view.response.ResponsePane
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}

class CardinalView(showAsCurl: () => Unit,
                   showErrorDialogCallback: String => Unit,
                   getConfigCallback: () => Config,
                   exportToCsv: List[(CardinalRequest, Option[CardinalResponse])] => Unit,
                   exportToBash: (List[CardinalRequest], Option[Long]) => Unit,
                   sendRequestCallback: CardinalRequest => CardinalResponse,
                   triggerUnsavedChangesMade: () => Unit) extends BorderPane {

  private val requestResponseSplitPane = new SplitPane()
  requestResponseSplitPane.setDividerPositions(0.4)

  private val requestInputPane = new RequestInputPane(triggerUnsavedChangesMade)
  requestInputPane.setMinWidth(400)
  private val responsePane = new ResponsePane(getConfigCallback, sendRequestCallback, exportToCsv, exportToBash, showErrorDialogCallback)
  responsePane.setMinWidth(400)
  private val requestControlPane = new RequestControlPane(showAsCurl, sendSingleRequest, showBulkRequestInput)

  val grid = new GridPane
  grid.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build)
  grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build)
  grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.NEVER).build)

  requestResponseSplitPane.getItems.add(requestInputPane)
  requestResponseSplitPane.getItems.add(responsePane)

  grid.add(requestResponseSplitPane, 0, 0)

//  GridPane.setColumnSpan(requestControlPane, 2)
  grid.add(requestControlPane, 0, 1)

  setCenter(grid)

  def clearAll(): Unit = {
    requestInputPane.clear()
    responsePane.clearContents()
  }

  private def sendSingleRequest(onStart: () => Unit, onFinish: () => Unit): Unit = {
    Platform.runLater(() => responsePane.clearContents())
    val request = requestInputPane.getRequest
    if (request.uri.trim.length == 0) {
      showErrorDialogCallback("Please enter a URL.")
    } else {
      new Thread() {
        override def run(): Unit = {
          Platform.runLater(() => onStart())
          val httpResponse = try {
            Some(sendRequestCallback(requestInputPane.getRequest.processConstants(getConfigCallback())))
          } catch {
            case _: ConnectException => showErrorDialogCallback("Connection refused."); None
            case _: URISyntaxException => showErrorDialogCallback("Invalid URL."); None
            case _: UnknownHostException => showErrorDialogCallback("Unknown Host."); None
            case _: SSLHandshakeException => showErrorDialogCallback("SSL Handshake failed. Remote host closed connection during handshake."); None
            case _: Exception => showErrorDialogCallback("Unknown error occurred."); None
          } finally {
            Platform.runLater(() => { onFinish() })
          }
          responsePane.setResponse(httpResponse)
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

  def loadRequest(request: CardinalRequest): Unit = requestInputPane.loadRequest(request)

  def loadCurlCommand(curl: String): Unit = responsePane.loadCurlCommand(curl)

  def getRequest: CardinalRequest = requestInputPane.getRequest

  def setBody(body: String): Unit = requestInputPane.setBody(Some(body))

  def addHeader(header: String): Unit = requestInputPane.addHeader(header)

}

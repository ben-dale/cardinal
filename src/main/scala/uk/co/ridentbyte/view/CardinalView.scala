package uk.co.ridentbyte.view

import java.net.{ConnectException, URISyntaxException, UnknownHostException}
import java.util.function

import javafx.application.Platform
import javafx.scene.control.SplitPane
import javafx.scene.layout.{BorderPane, GridPane, Priority}
import javax.net.ssl.SSLHandshakeException
import uk.co.ridentbyte.model.{CardinalRequest, CardinalResponse, Config}
import uk.co.ridentbyte.view.request.{RequestControlPane, RequestInputPane}
import uk.co.ridentbyte.view.response.ResponsePane
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}

import scala.runtime.BoxedUnit

class CardinalView(showAsCurl: java.util.function.Function[Void, Void],
                   showErrorDialogCallback: String => BoxedUnit,
                   getConfigCallback: () => Config,
                   exportToCsv: List[(CardinalRequest, Option[CardinalResponse])] => Unit,
                   exportToBash: (List[CardinalRequest], Option[Long]) => Unit,
                   sendRequestCallback: CardinalRequest => CardinalResponse,
                   triggerUnsavedChangesMade: java.util.function.Function[Void, Void]) extends BorderPane {

  private val requestResponseSplitPane = new SplitPane()
  requestResponseSplitPane.setDividerPositions(0.4)

  private val requestInputPane = new RequestInputPane(triggerUnsavedChangesMade)
  requestInputPane.setMinWidth(400)
  private val responsePane = new ResponsePane(getConfigCallback, sendRequestCallback, exportToCsv, exportToBash, showErrorDialogCallback)
  responsePane.setMinWidth(400)
  private val requestControlPane = new RequestControlPane(showAsCurl, showBulkRequestInput, sendSingleRequest)

  val grid = new GridPane
  grid.getColumnConstraints.add(new ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build)
  grid.getRowConstraints.add(new RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build)
  grid.getRowConstraints.add(new RowConstraintsBuilder().withVgrow(Priority.NEVER).build)

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

  private def sendSingleRequest(onStart: () => BoxedUnit, onFinish: () => BoxedUnit): BoxedUnit = {
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
            case e: Exception =>
              e.printStackTrace()
              showErrorDialogCallback("Unknown error occurred."); None
          } finally {
            Platform.runLater(() => { onFinish() })
          }
          responsePane.setResponse(httpResponse)
        }
      }.start()
      scala.runtime.BoxedUnit.UNIT
    }
  }

  private def showBulkRequestInput: java.util.function.Function[Void, Void] = {
    new function.Function[Void, Void] {
      override def apply(t: Void): Void = {
        val request = requestInputPane.getRequest
        if (request.uri.trim.length == 0) {
          showErrorDialogCallback("Please enter a URL.")
          null
        } else {
          responsePane.showBulkRequestInput(requestInputPane.getRequest)
          scala.runtime.BoxedUnit.UNIT
          null
        }
      }
    }
  }

  def loadRequest(request: CardinalRequest): Unit = requestInputPane.loadRequest(request)

  def loadCurlCommand(curl: String): Unit = responsePane.loadCurlCommand(curl)

  def getRequest: CardinalRequest = requestInputPane.getRequest

  def setBody(body: String): Unit = requestInputPane.setBody(body)

  def addHeader(header: String): Unit = requestInputPane.addHeader(header)

}

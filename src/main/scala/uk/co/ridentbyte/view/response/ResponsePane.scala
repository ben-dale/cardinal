package uk.co.ridentbyte.view.response

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.layout._
import uk.co.ridentbyte.model._

class ResponsePane(getConfigCallback: java.util.function.Function[Void, Config],
                   sendRequestCallback: java.util.function.Function[CardinalRequest, CardinalResponse],
                   exportToCsv: java.util.function.Function[java.util.List[CardinalRequestAndResponse], Void],
                   exportToBash: java.util.function.BiFunction[java.util.List[CardinalRequest], java.lang.Integer, Void],
                   showErrorDialogCallback: java.util.function.Function[String, Void]) extends BorderPane {

  setPadding(new Insets(20, 20, 20, 20))
  clearContents()

  def clearContents(): Unit = {
    setCenter(new ClearResponsePane())
  }

  def setResponse(response: CardinalResponse): Unit = {
    Platform.runLater(() => setCenter(new ResponseOutputPane(response)))
  }

  def loadCurlCommand(command: String): Unit = {
    Platform.runLater(() => setCenter(new CurlOutputPane(command)))
  }

  def showBulkRequestInput(request: CardinalRequest): Unit = {
    Platform.runLater(() => setCenter(BulkRequestInputPane(getConfigCallback, exportToBash, startBulkRequest, showErrorDialogCallback, request)))
  }

  def startBulkRequest(bulkRequest: CardinalBulkRequest): Unit = {
    if (bulkRequest.getRequestCount == 0 || bulkRequest.getIds.isEmpty) {
      showErrorDialogCallback.apply("Invalid input. \nPlease provide a throttle and either a request count or a range value.")
    } else {
      val outputPane = BulkRequestProcessingOutputPane(getConfigCallback, sendRequestCallback, finishedBulkRequestCallback, bulkRequest)
      Platform.runLater(() => setCenter(outputPane))
      new Thread(outputPane.task).start()
    }
  }

  def finishedBulkRequestCallback(requestAndResponses: List[CardinalRequestAndResponse], throttle: Int): Unit = {
    Platform.runLater(() =>
      setCenter(BulkRequestOutputPane(requestAndResponses, exportToCsv, exportToBash, throttle))
    )
  }

}

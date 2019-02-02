package uk.co.ridentbyte.view.response

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.layout._
import uk.co.ridentbyte.model._

class ResponsePane(getConfigCallback: () => Config,
                   sendRequestCallback: CardinalRequest => CardinalResponse,
                   exportToCsv: List[(CardinalRequest, Option[CardinalResponse])] => Unit,
                   exportToBash: (List[CardinalRequest], Option[Long]) => Unit,
                   showErrorDialogCallback: java.util.function.Function[String, Void]) extends BorderPane {

  setPadding(new Insets(20, 20, 20, 20))
  clearContents()

  def clearContents(): Unit = {
    setCenter(new ClearResponsePane())
  }

  def setResponse(response: Option[CardinalResponse]): Unit = {
    if (response.isDefined) {
      Platform.runLater(() => setCenter(new ResponseOutputPane(response.get)))
    }
  }

  def loadCurlCommand(command: String): Unit = {
    Platform.runLater(() => setCenter(new CurlOutputPane(command)))
  }

  def showBulkRequestInput(request: CardinalRequest, bulkRequest: Option[BulkRequest] = None): Unit = {
    Platform.runLater(() => setCenter(BulkRequestInputPane(getConfigCallback, exportToBash, startBulkRequest, request)))
  }

  def startBulkRequest(request: CardinalRequest, throttle: Option[Long], requestCount: Option[Int], ids: Option[List[String]]): Unit = {
    if (throttle.isEmpty || (requestCount.isEmpty && ids.isEmpty)) {
      showErrorDialogCallback.apply("Invalid input. \nPlease provide a throttle and either a request count or a range value.")
    } else {
      val outputPane = BulkRequestProcessingOutputPane(getConfigCallback, sendRequestCallback, finishedBulkRequestCallback, request, throttle, requestCount, ids)
      Platform.runLater(() => setCenter(outputPane))
      new Thread(outputPane.task).start()
    }
  }

  def finishedBulkRequestCallback(requestAndResponses: List[(CardinalRequest, Option[CardinalResponse])], throttle: Option[Long]): Unit = {
    Platform.runLater(() =>
      setCenter(BulkRequestOutputPane(requestAndResponses, exportToCsv, exportToBash, throttle))
    )
  }

}

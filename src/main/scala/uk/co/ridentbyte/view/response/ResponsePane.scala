package uk.co.ridentbyte.view.response

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.layout._
import uk.co.ridentbyte.model.{BulkRequest, Config, CardinalResponse, CardinalRequest}
import uk.co.ridentbyte.view.dialog.BulkRequestInputDialog

class ResponsePane(getConfigCallback: () => Config,
                   sendRequestCallback: CardinalRequest => CardinalResponse,
                   exportToCsv: List[(CardinalRequest, Option[CardinalResponse])] => Unit,
                   exportToBash: (List[CardinalRequest], Option[Long]) => Unit,
                   showErrorDialogCallback: String => Unit) extends BorderPane {

  setPadding(new Insets(20, 20, 20, 20))
  clearContents()

  def clearContents(): Unit = {
    setCenter(ClearResponsePane())
  }

  def setResponse(response: Option[CardinalResponse]): Unit = {
    if (response.isDefined) {
      Platform.runLater(() => setCenter(ResponseOutputPane(response.get)))
    }
  }

  def loadCurlCommand(command: String): Unit = {
    Platform.runLater(() => setCenter(CurlOutputPane(command)))
  }

  def showBulkRequestInput(request: CardinalRequest, bulkRequest: Option[BulkRequest] = None): Unit = {
    val bulkRequestInputDialog = new BulkRequestInputDialog(bulkRequest)
    val bulkRequestResult = bulkRequestInputDialog.showAndWait()
    if (bulkRequestResult != null && bulkRequestResult.isPresent) {
      if (bulkRequestResult.get.throttle.isEmpty || (bulkRequestResult.get.count.isEmpty && bulkRequestResult.get.ids.isEmpty)) {
        showErrorDialogCallback("Invalid input. \nPlease provide a throttle and either a request count or a range value.")
        showBulkRequestInput(request, Some(bulkRequestResult.get))
      } else if (bulkRequestResult.get.asBash) {
        asScript(request, bulkRequestResult.get.throttle, bulkRequestResult.get.count, bulkRequestResult.get.ids)
      } else {
        startBulkRequest(request, bulkRequestResult.get.throttle, bulkRequestResult.get.count, bulkRequestResult.get.ids)
      }
    }
  }

  def asScript(request: CardinalRequest, throttle: Option[Long], requestCount: Option[Int], ids: Option[List[String]]): Unit = {
    if (requestCount.isDefined) {
      val requests = 0 until requestCount.get map { i =>
        request.withId(i.toString).processConstants(getConfigCallback())
      }
      exportToBash(requests.toList, throttle)
    } else if (ids.isDefined) {
      val requests = ids.get.zipWithIndex.map { case (id, i) =>
        request.withId(id).processConstants(getConfigCallback())
      }
      exportToBash(requests, throttle)
    }
  }

  def startBulkRequest(request: CardinalRequest, throttle: Option[Long], requestCount: Option[Int], ids: Option[List[String]]): Unit = {
    val outputPane = BulkRequestProcessingOutputPane(getConfigCallback, sendRequestCallback, finishedBulkRequestCallback, request, throttle, requestCount, ids)
    Platform.runLater(() => setCenter(outputPane))
    new Thread(outputPane.task).start()
  }

  def finishedBulkRequestCallback(requestAndResponses: List[(CardinalRequest, Option[CardinalResponse])], throttle: Option[Long]): Unit = {
    Platform.runLater(() =>
      setCenter(BulkRequestOutputPane(requestAndResponses, exportToCsv, exportToBash, throttle))
    )
  }

}

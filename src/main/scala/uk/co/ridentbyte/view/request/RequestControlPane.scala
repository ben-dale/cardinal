package uk.co.ridentbyte.view.request

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.geometry.{HPos, Insets}
import javafx.scene.control.Button
import javafx.scene.layout._

class RequestControlPane(sendRequestCallback: () => Unit, showBulkRequestCallback: () => Unit) extends GridPane {

  setHgap(10)
  setVgap(10)
  setPadding(new Insets(0, 10, 10, 10))

  private val buttonSendBulkRequest = new Button("Send Bulk Request...")
  GridPane.setVgrow(buttonSendBulkRequest, Priority.NEVER)
  GridPane.setHgrow(buttonSendBulkRequest, Priority.ALWAYS)
  GridPane.setHalignment(buttonSendBulkRequest, HPos.RIGHT)
  buttonSendBulkRequest.setOnAction((_) => showBulkRequestCallback())
  add(buttonSendBulkRequest, 0, 0)

  private val buttonSendRequest = new Button("Send Request")
  GridPane.setVgrow(buttonSendRequest, Priority.NEVER)
  GridPane.setHgrow(buttonSendRequest, Priority.NEVER)
  buttonSendRequest.setOnAction((_) => {
    val task = new Task[Unit] {
      override def call(): Unit = {
        buttonSendRequest.setDisable(true)
        sendRequestCallback()
      }

      override def done(): Unit = {
        super.done()
        buttonSendRequest.setDisable(false)
      }
    }

    Platform.runLater(() => new Thread(task).start())
  })
  add(buttonSendRequest, 1, 0)

}

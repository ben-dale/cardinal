package uk.co.ridentbyte.view.request

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.geometry.{HPos, Insets}
import javafx.scene.control.Button
import javafx.scene.layout._

class RequestControlPane(sendRequestCallback: () => Unit,
                         sendBulkRequestCallback: () => Unit,
                         clearAllCallback: () => Unit,
                         saveCallback: () => Unit) extends GridPane {

  setHgap(10)
  setVgap(10)
  setPadding(new Insets(0, 10, 0, 0))

//  setGridLinesVisible(true)

  private val buttonClear = new Button("Clear All")
  GridPane.setVgrow(buttonClear, Priority.NEVER)
  GridPane.setHgrow(buttonClear, Priority.ALWAYS)
  GridPane.setHalignment(buttonClear, HPos.RIGHT)
  buttonClear.setOnAction((_) => clearAllCallback())
  add(buttonClear, 0, 0)

  private val buttonSave = new Button("Save")
  GridPane.setVgrow(buttonSave, Priority.NEVER)
  GridPane.setHgrow(buttonSave, Priority.NEVER)
//  GridPane.setHalignment(buttonSave, HPos.RIGHT)
  buttonSave.setOnAction((_) => saveCallback())
  add(buttonSave, 1, 0)
//
//  private val buttonAsCurl = new Button("As cURL...")
//  GridPane.setVgrow(buttonAsCurl, Priority.NEVER)
//  GridPane.setHgrow(buttonAsCurl, Priority.ALWAYS)
//  GridPane.setHalignment(buttonAsCurl, HPos.RIGHT)
//  buttonAsCurl.setOnAction((_) => println("As CURL clicked"))
//  add(buttonAsCurl, 0, 1)

  private val buttonSendBulkRequest = new Button("Send Bulk Request...")
  GridPane.setVgrow(buttonSendBulkRequest, Priority.NEVER)
  GridPane.setHgrow(buttonSendBulkRequest, Priority.NEVER)
  buttonSendBulkRequest.setOnAction((_) => sendBulkRequestCallback())
  add(buttonSendBulkRequest, 2, 0)

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
  add(buttonSendRequest, 3, 0)

}

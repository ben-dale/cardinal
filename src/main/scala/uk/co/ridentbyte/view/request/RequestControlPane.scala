package uk.co.ridentbyte.view.request

import javafx.geometry.{HPos, Insets}
import javafx.scene.control.Button
import javafx.scene.layout._

class RequestControlPane(sendRequestCallback: () => Unit,
                         clearAllCallback: () => Unit,
                         saveCallback: () => Unit) extends GridPane {

  setStyle(
    """
      |-fx-border-width: 1 0 0 1;
      |-fx-border-color: #DDDDDD;
      |-fx-border-style: solid hidden hidden solid;
    """.stripMargin
  )

  setHgap(10)
  setVgap(10)
  setPadding(new Insets(10, 10, 10, 10))

  private val buttonNew = new Button("New")
  GridPane.setVgrow(buttonNew, Priority.NEVER)
  GridPane.setHgrow(buttonNew, Priority.NEVER)
  buttonNew.setOnAction((_) => clearAllCallback())
  add(buttonNew, 0, 0)

  private val buttonSave = new Button("Save")
  GridPane.setVgrow(buttonSave, Priority.NEVER)
  GridPane.setHgrow(buttonSave, Priority.NEVER)
  buttonSave.setOnAction((_) => saveCallback())
  add(buttonSave, 1, 0)

  private val buttonSendRequest = new Button("Send Request")
  GridPane.setVgrow(buttonSendRequest, Priority.NEVER)
  GridPane.setHgrow(buttonSendRequest, Priority.ALWAYS)
  GridPane.setHalignment(buttonSendRequest, HPos.RIGHT)
  buttonSendRequest.setOnAction((_) => sendRequestCallback())
  add(buttonSendRequest, 2, 0)

}

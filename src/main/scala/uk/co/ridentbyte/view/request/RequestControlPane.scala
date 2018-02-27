package uk.co.ridentbyte.view.request

import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.layout._
import javafx.scene.paint.Color

class RequestControlPane(sendRequestCallback: () => Unit,
                         clearAllCallback: () => Unit,
                         saveCallback: () => Unit) extends GridPane {

  setHgap(10)
  setVgap(10)
  setPadding(new Insets(10, 10, 10, 10))

  setBackground(new Background(new BackgroundFill(Color.web("#DDDDDD"), CornerRadii.EMPTY, Insets.EMPTY)))

  val buttonClearAll = new Button("Clear All")
  buttonClearAll.setOnAction((_) => clearAllCallback())
  add(buttonClearAll, 0, 0)

  val buttonSendRequest = new Button("Send Request")
  buttonSendRequest.setOnAction((_) => sendRequestCallback())
  add(buttonSendRequest, 1, 0)

  val buttonSave = new Button("Save")
  buttonSave.setOnAction((_) => saveCallback())
  add(buttonSave, 2, 0)


}

package uk.co.ridentbyte.view.request

import javafx.geometry.{HPos, Insets, VPos}
import javafx.scene.control.Button
import javafx.scene.layout.{Background, BackgroundFill, CornerRadii, GridPane}
import javafx.scene.paint.Color

class RequestControlPane(sendRequestCallback: () => Unit, addHeaderCallback: () => Unit) extends GridPane {

  setHgap(10)
  setPadding(new Insets(10, 10, 10, 10))
  setBackground(new Background(new BackgroundFill(Color.web("grey"), CornerRadii.EMPTY, Insets.EMPTY)))

  val buttonAddHeader = new Button("Add Header...")
  buttonAddHeader.setOnAction((_) => addHeaderCallback())
  add(buttonAddHeader, 0, 0)

  val buttonSendRequest = new Button("Send Request")
  buttonSendRequest.setOnAction((_) => sendRequestCallback())
  add(buttonSendRequest, 1, 0)
}

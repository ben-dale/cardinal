package uk.co.ridentbyte.view.request

import javafx.geometry.{HPos, Insets, VPos}
import javafx.scene.control.Button
import javafx.scene.layout.{Background, BackgroundFill, CornerRadii, GridPane}
import javafx.scene.paint.Color

class RequestControlPane(sendRequestCallback: () => Unit) extends GridPane {

  setVgap(10)
  setPadding(new Insets(10, 10, 10, 10))
  setBackground(new Background(new BackgroundFill(Color.web("#00FF00"), CornerRadii.EMPTY, Insets.EMPTY)))

  val buttonSendRequest = new Button("Send Request")
  buttonSendRequest.setOnAction((_) => sendRequestCallback())
  GridPane.setValignment(buttonSendRequest, VPos.TOP)
  GridPane.setHalignment(buttonSendRequest, HPos.RIGHT)
  add(buttonSendRequest, 0, 0)
}

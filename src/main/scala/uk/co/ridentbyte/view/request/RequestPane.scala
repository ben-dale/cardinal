package uk.co.ridentbyte.view.request

import javafx.geometry.{Insets, VPos}
import javafx.scene.layout._
import javafx.scene.paint.Color

class RequestPane(sendRequestCallback: (String, String) => Unit) extends GridPane {

  setBackground(new Background(new BackgroundFill(Color.web("#FF0000"), CornerRadii.EMPTY, Insets.EMPTY)))

  setStyle(
    """
      |-fx-border-width: 0 1 0 0;
      |-fx-border-color: grey;
      |-fx-border-style: hidden solid hidden hidden;
    """.stripMargin)

  val requestInputPane = new RequestInputPane
  GridPane.setHgrow(requestInputPane, Priority.ALWAYS)
  add(requestInputPane, 0, 0)

  val requestControlPane = new RequestControlPane(sendRequest)
  GridPane.setHgrow(requestControlPane, Priority.ALWAYS)
  GridPane.setValignment(requestControlPane, VPos.BASELINE)
  add(requestControlPane, 0, 1)

  def sendRequest(): Unit = {
    val verb = requestInputPane.getVerb
    val uri = requestInputPane.getUri
    sendRequestCallback(verb, uri)
  }

}

package uk.co.ridentbyte.view.request

import javafx.scene.control.{Label, TextArea}
import javafx.scene.layout.{GridPane, Priority}

class RequestHeadersInputPane(triggerUnsavedChangesMade: () => Unit) extends GridPane {

  setHgap(5)
  setVgap(5)

  private val labelHeaders = new Label("Headers")
  labelHeaders.getStyleClass.add("cardinal-font")
  GridPane.setColumnSpan(labelHeaders, 2)
  GridPane.setVgrow(labelHeaders, Priority.NEVER)
  GridPane.setHgrow(labelHeaders, Priority.ALWAYS)
  add(labelHeaders, 0, 0)

  private val textHeaders = new TextArea
  textHeaders.textProperty().addListener((_, _, _) => triggerUnsavedChangesMade())
  textHeaders.getStyleClass.add("cardinal-font")
  GridPane.setVgrow(textHeaders, Priority.ALWAYS)
  GridPane.setHgrow(textHeaders, Priority.ALWAYS)
  add(textHeaders, 0, 1)

  def getHeaders: List[String] = {
    textHeaders.getText.split("\n").toList
  }

  def clear(): Unit = {
    textHeaders.setText("")
  }

  def addHeader(header: String): Unit = {
    if (textHeaders.getText.trim.isEmpty) {
      textHeaders.setText(header)
    } else {
      textHeaders.setText(textHeaders.getText + "\n" + header)
    }
  }
}

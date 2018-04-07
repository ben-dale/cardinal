package uk.co.ridentbyte.view.request

import javafx.scene.control.{Label, TextArea}
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout._

class RequestBodyInputPane(triggerUnsavedChangesMade: () => Unit) extends GridPane {

  setHgap(5)
  setVgap(5)

  private val labelBody = new Label("Body")
  labelBody.getStyleClass.add("cardinal-font")
  GridPane.setVgrow(labelBody, Priority.NEVER)
  GridPane.setHgrow(labelBody, Priority.ALWAYS)
  add(labelBody, 0, 0)

  private val textAreaBody = new TextArea()
  textAreaBody.textProperty().addListener((_, _, _) => triggerUnsavedChangesMade())
  textAreaBody.addEventFilter(KeyEvent.KEY_PRESSED, (e: KeyEvent) => {
    if (e.getCode == KeyCode.TAB) {
      textAreaBody.insertText(textAreaBody.getCaretPosition, "  ")
      e.consume()
    }
  })
  textAreaBody.getStyleClass.add("cardinal-font")
  GridPane.setVgrow(textAreaBody, Priority.ALWAYS)
  GridPane.setHgrow(textAreaBody, Priority.ALWAYS)
  add(textAreaBody, 0, 1)

  def setBody(body: Option[String]): Unit = {
    body.foreach(textAreaBody.setText(_))
  }

  def getBody: Option[String] = {
    val contents = textAreaBody.getText.trim
    if (contents.length == 0) None else Some(contents)
  }

  def clear(): Unit = {
    textAreaBody.clear()
  }

}

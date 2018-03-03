package uk.co.ridentbyte.view.request

import javafx.scene.control.{Label, TextArea}
import javafx.scene.layout._

class RequestBodyInputPane extends GridPane {

  setHgap(5)
  setVgap(5)

  private val labelBody = new Label("Body")
  labelBody.setStyle(labelStyle)
  GridPane.setVgrow(labelBody, Priority.NEVER)
  GridPane.setHgrow(labelBody, Priority.ALWAYS)
  add(labelBody, 0, 0)

  private val textAreaBody = new TextArea()
  GridPane.setVgrow(textAreaBody, Priority.ALWAYS)
  GridPane.setHgrow(textAreaBody, Priority.ALWAYS)
  add(textAreaBody, 0, 1)

  private def labelStyle: String = {
    """
      |-fx-font-size: 12;
    """.stripMargin
  }

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

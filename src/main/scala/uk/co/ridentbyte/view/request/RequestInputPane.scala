package uk.co.ridentbyte.view.request

import javafx.geometry.Insets
import javafx.scene.control.{Label, TextArea}
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout._
import uk.co.ridentbyte.model.CardinalRequest
import uk.co.ridentbyte.view.util.RowConstraintsBuilder

class RequestInputPane(triggerUnsavedChangesMade: () => Unit) extends GridPane {

  private var currentRequest: Option[CardinalRequest] = None

  setHgap(10)
  setVgap(10)
  setPadding(new Insets(20))

  getRowConstraints.addAll(
    RowConstraintsBuilder().build,
    RowConstraintsBuilder().build,
    RowConstraintsBuilder().withMaxHeight(200).build,
    RowConstraintsBuilder().build,
    RowConstraintsBuilder().build
  )

  private val uriVerbInputPane = new RequestUriVerbInputPane(triggerUnsavedChangesMade)
  GridPane.setVgrow(uriVerbInputPane, Priority.NEVER)
  GridPane.setHgrow(uriVerbInputPane, Priority.ALWAYS)
  add(uriVerbInputPane, 0, 0)

  private val labelHeaders = new Label("Headers")
  labelHeaders.getStyleClass.add("cardinal-font")
  GridPane.setVgrow(labelHeaders, Priority.NEVER)
  GridPane.setHgrow(labelHeaders, Priority.ALWAYS)
  add(labelHeaders, 0, 1)

  private val textHeaders = new TextArea
  textHeaders.textProperty().addListener((_, _, _) => triggerUnsavedChangesMade())
  textHeaders.getStyleClass.add("cardinal-font")
  GridPane.setVgrow(textHeaders, Priority.ALWAYS)
  GridPane.setHgrow(textHeaders, Priority.ALWAYS)
  add(textHeaders, 0, 2)

  private val labelBody = new Label("Body")
  labelBody.getStyleClass.add("cardinal-font")
  GridPane.setVgrow(labelBody, Priority.NEVER)
  GridPane.setHgrow(labelBody, Priority.ALWAYS)
  add(labelBody, 0, 3)

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
  add(textAreaBody, 0, 4)

  def setBody(body: Option[String]): Unit = {
    body.foreach(textAreaBody.setText)
  }

  def getBody: Option[String] = {
    val contents = textAreaBody.getText.trim
    if (contents.length == 0) None else Some(contents)
  }

  def getHeaders: List[String] = {
    textHeaders.getText.split("\n").toList
  }

  def clear(): Unit = {
    currentRequest = None
    uriVerbInputPane.clear()
  }

  def loadRequest(request: CardinalRequest): Unit = {
    currentRequest = Some(request)
    setBody(request.body)
    textHeaders.setText(request.headers.mkString("\n"))
    uriVerbInputPane.setVerb(request.verb)
    uriVerbInputPane.setUri(request.uri)
  }

  def getRequest: CardinalRequest = {
    val body = getBody
    val headers = getHeaders
    val verb = uriVerbInputPane.getVerb
    val uri = uriVerbInputPane.getUri
    CardinalRequest(uri, verb, headers, body)
  }

  def addHeader(header: String): Unit = {
    if (textHeaders.getText.trim.isEmpty) {
      textHeaders.setText(header)
    } else {
      textHeaders.setText(textHeaders.getText + "\n" + header)
    }
  }

}

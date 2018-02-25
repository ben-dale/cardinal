package uk.co.ridentbyte.view.request

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control._
import javafx.scene.layout._

import scala.collection.JavaConverters._

class RequestInputPane extends GridPane {

  private var unsavedChanges: Boolean = false

  setHgap(10)
  setVgap(10)
  setPadding(new Insets(10, 10, 10, 10))

  val maxHeightRowContstraint = new RowConstraints()
  maxHeightRowContstraint.setMaxHeight(220)

  getRowConstraints.addAll(
    new RowConstraints(),
    new RowConstraints(),
    maxHeightRowContstraint,
    new RowConstraints()
  )

  val textFilename = new TextField()
  textFilename.setOnKeyPressed((_) => onEdit())
  GridPane.setVgrow(textFilename, Priority.NEVER)
  GridPane.setHgrow(textFilename, Priority.ALWAYS)
  GridPane.setColumnSpan(textFilename, 2)
  textFilename.setPromptText("example_filename")
  add(textFilename, 0, 0)

  val textUri = new TextField()
  textUri.setOnKeyPressed((_) => onEdit())
  GridPane.setVgrow(textUri, Priority.NEVER)
  GridPane.setHgrow(textUri, Priority.ALWAYS)
  textUri.setPromptText("http://localhost:8080")
  add(textUri, 0, 1)

  val selectVerb = new ChoiceBox[String](FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE", "HEAD", "CONNECT", "OPTIONS", "TRACE", "PATCH"))
  selectVerb.setOnMouseClicked((_) => onEdit())
  selectVerb.setOnKeyPressed((_) => onEdit())
  GridPane.setVgrow(selectVerb, Priority.NEVER)
  GridPane.setHgrow(selectVerb, Priority.NEVER)
  selectVerb.getSelectionModel.selectFirst()
  add(selectVerb, 1, 1)

  val headersInputPane = new RequestHeadersInputPane(onEdit)
  GridPane.setVgrow(headersInputPane, Priority.ALWAYS)
  GridPane.setHgrow(headersInputPane, Priority.ALWAYS)
  GridPane.setColumnSpan(headersInputPane, 2)
  add(headersInputPane, 0, 2)

  val bodyInputPane = new RequestBodyInputPane(onEdit)
  GridPane.setVgrow(bodyInputPane, Priority.ALWAYS)
  GridPane.setHgrow(bodyInputPane, Priority.ALWAYS)
  GridPane.setColumnSpan(bodyInputPane, 2)
  add(bodyInputPane, 0, 3)

  def setUri(uri: String): Unit = textUri.setText(uri)
  def getUri: String = textUri.getText.trim

  def setVerb(verb: String): Unit = {
    val matchingIndex = selectVerb.getItems.asScala.zipWithIndex.find {
      case (item: String, _) => item == verb
    }.map { verbWithIndex =>
      verbWithIndex._2
    }.getOrElse(0)
    selectVerb.getSelectionModel.select(matchingIndex)
  }
  def getVerb: String = selectVerb.getSelectionModel.getSelectedItem

  def setFilename(filename: String): Unit = textFilename.setText(filename)
  def getFilename: String = textFilename.getText.trim

  def addHeaders(headers: List[String]): Unit = headers.foreach(addHeader)
  def addHeader(header: String): Unit = headersInputPane.addHeader(header)

  def setBody(body: Option[String]): Unit = {
    bodyInputPane.setBody(body)
  }

  def getBody: Option[String] = {
    bodyInputPane.getBody
  }

  def getHeaders: List[String] = {
    headersInputPane.getHeaders
  }

  def clear(): Unit = {
    unsavedChanges = false
    textFilename.clear()
    textUri.clear()
    selectVerb.getSelectionModel.select(0)
    headersInputPane.clear()
    bodyInputPane.clear()
  }

  def hasUnsavedChanges: Boolean = unsavedChanges

  def setUnsavedChanges(unsavedChanges: Boolean): Unit = {
    this.unsavedChanges = unsavedChanges
  }

  def onEdit(): Unit = {
    setUnsavedChanges(true)
  }

}

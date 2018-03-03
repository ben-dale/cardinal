package uk.co.ridentbyte.view.request

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control._
import javafx.scene.layout._

import uk.co.ridentbyte.model.Request

import scala.collection.JavaConverters._

class RequestInputPane extends GridPane {

  private var currentRequest: Option[Request] = None

  setStyle(
    """
      |-fx-border-width: 0 1 0 1;
      |-fx-border-color: #DDDDDD;
      |-fx-border-style: hidden solid hidden solid;
    """.stripMargin
  )

  setHgap(10)
  setVgap(10)
  setPadding(new Insets(10, 10, 10, 10))

  val maxHeightRowContstraint = new RowConstraints()
  maxHeightRowContstraint.setMaxHeight(220)

  getRowConstraints.addAll(
    new RowConstraints(),
    maxHeightRowContstraint,
    new RowConstraints()
  )

  val textUri = new TextField()
  GridPane.setVgrow(textUri, Priority.NEVER)
  GridPane.setHgrow(textUri, Priority.ALWAYS)
  textUri.setPromptText("http://localhost:8080")
  add(textUri, 0, 0)

  val selectVerb = new ChoiceBox[String](FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE", "HEAD", "CONNECT", "OPTIONS", "TRACE", "PATCH"))
  GridPane.setVgrow(selectVerb, Priority.NEVER)
  GridPane.setHgrow(selectVerb, Priority.NEVER)
  selectVerb.getSelectionModel.selectFirst()
  add(selectVerb, 1, 0)

  val headersInputPane = new RequestHeadersInputPane
  GridPane.setVgrow(headersInputPane, Priority.ALWAYS)
  GridPane.setHgrow(headersInputPane, Priority.ALWAYS)
  GridPane.setColumnSpan(headersInputPane, 2)
  add(headersInputPane, 0, 1)

  val bodyInputPane = new RequestBodyInputPane
  GridPane.setVgrow(bodyInputPane, Priority.ALWAYS)
  GridPane.setHgrow(bodyInputPane, Priority.ALWAYS)
  GridPane.setColumnSpan(bodyInputPane, 2)
  add(bodyInputPane, 0, 2)

  def setVerb(verb: String): Unit = {
    val matchingIndex = selectVerb.getItems.asScala.zipWithIndex.find {
      case (item: String, _) => item == verb
    }.map { verbWithIndex =>
      verbWithIndex._2
    }.getOrElse(0)
    selectVerb.getSelectionModel.select(matchingIndex)
  }

  def clear(): Unit = {
    currentRequest = None
    textUri.clear()
    selectVerb.getSelectionModel.select(0)
    headersInputPane.clear()
    bodyInputPane.clear()
  }

  def loadRequest(request: Request): Unit = {
    currentRequest = Some(request)
    bodyInputPane.setBody(request.body)
    request.headers.foreach(headersInputPane.addHeader)
    setVerb(request.verb)
    textUri.setText(request.uri)
  }

  def getRequest: Request = {
    val body = bodyInputPane.getBody
    val headers = headersInputPane.getHeaders
    val verb = selectVerb.getSelectionModel.getSelectedItem
    val fileName = if (currentRequest.isDefined) currentRequest.get.name else None
    val uri = textUri.getText.trim
    Request(fileName, uri, verb, headers, body)
  }

}

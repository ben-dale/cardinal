package uk.co.ridentbyte.view.request

import javafx.scene.layout.{GridPane, Priority}

import uk.co.ridentbyte.view.util.RowConstraintsBuilder

class RequestHeadersBodyInputPane extends GridPane {

  setHgap(5)
  setVgap(5)

  getRowConstraints.addAll(
    RowConstraintsBuilder().withMaxHeight(250).build,
    RowConstraintsBuilder().build
  )

  private val headersInputPane = new RequestHeadersInputPane
  GridPane.setVgrow(headersInputPane, Priority.ALWAYS)
  GridPane.setHgrow(headersInputPane, Priority.ALWAYS)
  add(headersInputPane, 0, 0)

  private val bodyInputPane = new RequestBodyInputPane
  GridPane.setVgrow(bodyInputPane, Priority.ALWAYS)
  GridPane.setHgrow(bodyInputPane, Priority.ALWAYS)
  add(bodyInputPane, 0, 1)

  def getBody: Option[String] = bodyInputPane.getBody

  def getHeaders: List[String] = headersInputPane.getHeaders

  def setBody(body: Option[String]): Unit = bodyInputPane.setBody(body)

  def setHeaders(headers: List[String]): Unit = {
    headersInputPane.clear()
    headers.foreach(headersInputPane.addHeader)
  }

  def addHeader(header: String): Unit = {
    headersInputPane.addHeader(header)
  }

  def clear(): Unit = {
    headersInputPane.clear()
    bodyInputPane.clear()
  }

}

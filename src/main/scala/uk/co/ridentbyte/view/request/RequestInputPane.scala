package uk.co.ridentbyte.view.request

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.control._
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout._

import scala.collection.JavaConverters._

class RequestInputPane extends GridPane {

  setHgap(10)
  setVgap(10)
  setPadding(new Insets(10, 10, 10, 10))

  val textUri = new TextField()
  GridPane.setVgrow(textUri, Priority.NEVER)
  GridPane.setHgrow(textUri, Priority.ALWAYS)
  textUri.setPromptText("http://localhost:8080")
  textUri.setText("https://reqres.in/api/users")
  add(textUri, 0, 0)

  val selectVerb = new ChoiceBox[String](FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE", "HEAD", "CONNECT", "OPTIONS", "TRACE", "PATCH"))
  GridPane.setVgrow(selectVerb, Priority.NEVER)
  GridPane.setHgrow(selectVerb, Priority.NEVER)
  selectVerb.getSelectionModel.selectFirst()
  add(selectVerb, 1, 0)

  val labelRowConstraint = new RowConstraints(5)
  labelRowConstraint.setVgrow(Priority.NEVER)

  val headerRowConstraint = new RowConstraints()
  headerRowConstraint.setMaxHeight(200)
  headerRowConstraint.setVgrow(Priority.ALWAYS)

  val bodyRowConstraint = new RowConstraints()
  bodyRowConstraint.setVgrow(Priority.ALWAYS)

  val gridHeadersBody = new GridPane
  gridHeadersBody.getRowConstraints.addAll(
    labelRowConstraint,
    headerRowConstraint,
    labelRowConstraint,
    bodyRowConstraint
  )
  gridHeadersBody.setVgap(10)
  GridPane.setVgrow(gridHeadersBody, Priority.ALWAYS)
  GridPane.setHgrow(gridHeadersBody, Priority.ALWAYS)
  GridPane.setColumnSpan(gridHeadersBody, 2)
  add(gridHeadersBody, 0, 1)

  val labelHeaders = new Label("Headers")
  labelHeaders.setStyle(labelStyle)
  GridPane.setVgrow(labelHeaders, Priority.NEVER)
  GridPane.setHgrow(labelHeaders, Priority.ALWAYS)
  GridPane.setColumnSpan(labelHeaders, 2)
  gridHeadersBody.add(labelHeaders, 0, 0)

  val listHeaders = new ListView[String]()
  listHeaders.setStyle(
    """
      |-fx-font-family: Monospaced;
      |-fx-font-size: 13;
      |-fx-font-weight: 600;
    """.stripMargin
  )
  listHeaders.setCellFactory(TextFieldListCell.forListView())
  listHeaders.addEventFilter(KeyEvent.KEY_RELEASED, (event: KeyEvent) => {
    val selectedIndex = listHeaders.getSelectionModel.getSelectedIndex
    if (event.getCode == KeyCode.BACK_SPACE && selectedIndex >= 0) {
      listHeaders.getItems.remove(listHeaders.getSelectionModel.getSelectedIndex)
    }
  })
  GridPane.setVgrow(listHeaders, Priority.ALWAYS)
  GridPane.setHgrow(listHeaders, Priority.ALWAYS)
  GridPane.setColumnSpan(listHeaders, 2)
  gridHeadersBody.add(listHeaders, 0, 1)

  val labelBody = new Label("Body")
  labelBody.setStyle(labelStyle)
  GridPane.setVgrow(labelBody, Priority.NEVER)
  GridPane.setHgrow(labelBody, Priority.ALWAYS)
  GridPane.setColumnSpan(labelBody, 2)
  gridHeadersBody.add(labelBody, 0, 2)

  val textAreaBody = new TextArea()
  textAreaBody.setText(
    """
      |{
      |    "name": "morpheus",
      |    "job": "leader"
      |}
    """.stripMargin)
  GridPane.setColumnSpan(textAreaBody, 2)
  GridPane.setHgrow(textAreaBody, Priority.ALWAYS)
  GridPane.setVgrow(textAreaBody, Priority.ALWAYS)
  gridHeadersBody.add(textAreaBody, 0, 3)


  def getUri: String = textUri.getText.trim

  def getVerb: String = selectVerb.getSelectionModel.getSelectedItem

  def addHeader(header: String): Unit = listHeaders.getItems.add(header)

  private def labelStyle: String = {
    """
      |-fx-font-size: 12;
    """.stripMargin
  }

  def getHeaders: List[String] = {
    listHeaders.getItems.asScala.toList
  }

  def getBody: Option[String] = {
    val contents = textAreaBody.getText.trim
    if (contents.length == 0) None else Some(contents)
  }

  def clear(): Unit = {
    textAreaBody.clear()
    listHeaders.getItems.clear()
    selectVerb.getSelectionModel.select(0)
    textUri.clear()
  }

}

package uk.co.ridentbyte.view.response

import javafx.geometry.Insets
import javafx.scene.control.{ListView, Tab, TabPane, TextArea}
import javafx.scene.layout.GridPane

import uk.co.ridentbyte.model.Header
import uk.co.ridentbyte.view.util.GridConstraints

class ResponsePane extends GridPane {

  getColumnConstraints.add(GridConstraints.maxWidthColumnConstraint)

  getRowConstraints.add(GridConstraints.noScaleRowConstraint)
  getRowConstraints.add(GridConstraints.maxHeightRowConstraint)

  val summaryPane = new ResponseSummaryPane

  val textAreaBody = new TextArea()
  textAreaBody.setEditable(false)
  textAreaBody.setStyle(
    """
      |-fx-font-family: Monospaced;
      |-fx-font-size: 13;
      |-fx-font-weight: 700;
      |-fx-text-fill: grey;
      |-fx-control-inner-background:#000000;
      |-fx-border-color:#000000;
    """.stripMargin)

  val listHeaders = new ListView[String]()

  val tabPaneBodyHeaders = new TabPane()

  val tabBody = new Tab("Body")
  tabBody.setClosable(false)
  tabBody.setContent(textAreaBody)
  tabPaneBodyHeaders.getTabs.add(tabBody)

  val tabHeaders = new Tab("Headers")
  tabHeaders.setClosable(false)
  tabHeaders.setContent(listHeaders)
  tabPaneBodyHeaders.getTabs.add(tabHeaders)

  add(summaryPane, 0, 0)
  add(tabPaneBodyHeaders, 0, 1)

  def loadResponse(code: Int, headers: Iterable[Header], body: String): Unit = {
    listHeaders.getItems.clear()
    headers.foreach { header => listHeaders.getItems.add(header.toString) }
    textAreaBody.setText(body)
    summaryPane.setHttpCode(code.toString)
  }

}




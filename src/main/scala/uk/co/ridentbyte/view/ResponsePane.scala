package uk.co.ridentbyte.view

import javafx.scene.control.{ListView, Tab, TabPane, TextArea}
import javafx.scene.layout.GridPane

import uk.co.ridentbyte.model.Header
import uk.co.ridentbyte.view.util.GridConstraints

class ResponsePane extends GridPane {

//  setGridLinesVisible(true)
  setHgap(10)
  setVgap(10)
  getColumnConstraints.add(GridConstraints.maxWidthRowConstraint)
  getRowConstraints.add(GridConstraints.maxHeightRowConstraint)

  val textAreaBody = new TextArea()
  textAreaBody.setEditable(false)

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

  add(tabPaneBodyHeaders, 0, 0)


  def loadResponse(headers: Iterable[Header], body: String): Unit = {
    listHeaders.getItems.clear()
    headers.foreach { header => listHeaders.getItems.add(header.toString) }
    textAreaBody.setText(body)
  }

}




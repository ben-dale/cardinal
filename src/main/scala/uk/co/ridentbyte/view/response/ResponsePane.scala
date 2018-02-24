package uk.co.ridentbyte.view.response

import javafx.scene.control.{ListView, Tab, TabPane, TextArea}
import javafx.scene.layout.{GridPane, Priority}

import uk.co.ridentbyte.model.HttpResponseWrapper

class ResponsePane extends GridPane {

  val summaryPane = new ResponseSummaryPane
  add(summaryPane, 0, 0)

  val textAreaBody = new TextArea()
  textAreaBody.setEditable(false)
  textAreaBody.setStyle(
    """
      |-fx-font-family: Monospaced;
      |-fx-font-size: 13;
      |-fx-font-weight: 600;
    """.stripMargin
  )

  val listHeaders = new ListView[String]()
  listHeaders.setStyle(
    """
      |-fx-font-family: Monospaced;
      |-fx-font-size: 13;
      |-fx-font-weight: 600;
    """.stripMargin
  )

  val tabPaneBodyHeaders = new TabPane()

  val tabBody = new Tab("Body")
  tabBody.setClosable(false)
  tabBody.setContent(textAreaBody)
  tabPaneBodyHeaders.getTabs.add(tabBody)

  val tabHeaders = new Tab("Headers")
  tabHeaders.setClosable(false)
  tabHeaders.setContent(listHeaders)
  tabPaneBodyHeaders.getTabs.add(tabHeaders)

  GridPane.setVgrow(tabPaneBodyHeaders, Priority.ALWAYS)
  GridPane.setHgrow(tabPaneBodyHeaders, Priority.ALWAYS)
  add(tabPaneBodyHeaders, 0, 1)

  def loadResponse(httpResponseWrapper: HttpResponseWrapper): Unit = {
    listHeaders.getItems.clear()
    httpResponseWrapper.httpResponse.headers.foreach { header => listHeaders.getItems.add(header.toString) }
    textAreaBody.setText(httpResponseWrapper.httpResponse.body)

    summaryPane.setHttpCode(httpResponseWrapper.httpResponse.header("Status").getOrElse(""))
    summaryPane.setTime(httpResponseWrapper.time)
  }

  def clear(): Unit = {
    listHeaders.getItems.clear()
    textAreaBody.clear()
    summaryPane.clear()
  }

}




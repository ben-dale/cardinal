package uk.co.ridentbyte.view.response

import javafx.geometry.{HPos, Insets}
import javafx.scene.control._
import javafx.scene.layout.{GridPane, Priority}
import uk.co.ridentbyte.model.{HttpResponseWrapper, Request}
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}

class ResponsePane(
                    startBulkRequest: (Request, Option[Long], Option[Int], Option[List[String]]) => Unit
                  ) extends GridPane {

  private val tabbedPane = new TabPane()
  GridPane.setVgrow(tabbedPane, Priority.ALWAYS)
  GridPane.setHgrow(tabbedPane, Priority.ALWAYS)
  add(tabbedPane, 0, 1)

  tabbedPane.getTabs.add(infoTab)

  def clear(): Unit = {
    tabbedPane.getTabs.remove(1, tabbedPane.getTabs.size())
  }

  def addResponse(response: HttpResponseWrapper): Unit = {
    val newTab = new Tab("HTTP " + response.httpResponse.code)

    val grid = new GridPane
    grid.setPadding(new Insets(10, 10, 10, 10))
    grid.setVgap(10)
    grid.setHgap(10)
    grid.getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build)
    grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).withPercentageHeight(55).build)
    grid.getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).withPercentageHeight(45).build)

    val textAreaBody = new TextArea()
    textAreaBody.setEditable(false)
    textAreaBody.setStyle(
      """
        |-fx-font-family: Monospaced;
        |-fx-font-size: 13;
        |-fx-font-weight: 600;
      """.stripMargin
    )
    textAreaBody.setText(response.httpResponse.body)
    grid.add(textAreaBody, 0, 0)

    val listHeaders = new ListView[String]()
    listHeaders.setStyle(
      """
        |-fx-font-family: Monospaced;
        |-fx-font-size: 13;
        |-fx-font-weight: 600;
      """.stripMargin
    )
    response.httpResponse.headers.foreach {
      header => listHeaders.getItems.add(header._1 + ": " + header._2.mkString(""))
    }
    grid.add(listHeaders, 0, 1)

    newTab.setContent(grid)
    tabbedPane.getTabs.add(newTab)
    tabbedPane.getSelectionModel.select(newTab)
  }

  def infoTab: Tab = {
    val tab = new Tab("Info")
    tab.setClosable(false)
    tab
  }

  def showBulkRequestInput(request: Request): Unit = {
    val bulkRequestTab = bulkRequestInput(request)
    tabbedPane.getTabs.add(bulkRequestTab)
    tabbedPane.getSelectionModel.select(bulkRequestTab)
  }

  def bulkRequestInput(request: Request): Tab = {
    val tab = new Tab("Bulk Request")

    val grid = new GridPane
    grid.setPadding(new Insets(10, 10, 10, 10))
    grid.setHgap(10)
    grid.setVgap(10)

    val labelDelay = new Label("Delay per request (ms)")
    GridPane.setHalignment(labelDelay, HPos.RIGHT)
    grid.add(labelDelay, 0, 0)

    val textDelay = new TextField
    textDelay.setText("500")
    grid.add(textDelay, 1, 0)

    val separator = new Separator()
    GridPane.setColumnSpan(separator, 2)
    grid.add(separator, 0, 1)

    val labelNumOfRequests = new Label("No. of requests")
    GridPane.setHalignment(labelNumOfRequests, HPos.RIGHT)
    grid.add(labelNumOfRequests, 0, 2)

    val textNumOfRequests = new TextField
    grid.add(textNumOfRequests, 1, 2)

    val labelOr = new Label("- OR -")
    GridPane.setColumnSpan(labelOr, 2)
    GridPane.setHalignment(labelOr, HPos.CENTER)
    grid.add(labelOr, 0, 3)

    val labelForEach = new Label("For each")
    GridPane.setHalignment(labelForEach, HPos.RIGHT)
    grid.add(labelForEach, 0, 4)

    val textForEach = new TextField
    textForEach.setPromptText("325, 454, 432 or 12..54")
    grid.add(textForEach, 1, 4)

    tab.setContent(grid)

    val buttonStart = new Button("Start")
    buttonStart.setOnAction((_) => {
      val optTextDelay = if (textDelay.getText.trim.length == 0) {
        None
      } else {
        try {
          Some(textDelay.getText.trim.toLong)
        } catch {
          case _: Exception => None
        }
      }

      val optNumRequests = if (textNumOfRequests.getText.trim.length == 0) {
        None
      } else {
        try {
          Some(textNumOfRequests.getText.trim.toInt)
        } catch {
          case _: Exception => None
        }
      }

      val optTextForEach = if (textForEach.getText.trim.length == 0) {
        None
      } else {
        val matcher = "([0-9]+)..([0-9]+)".r
        textForEach.getText.trim match {
          case matcher(a, b) => Some(a.toInt.to(b.toInt).map(_.toString).toList)
          case v => Some(v.split(",").toList)
        }
      }
      startBulkRequest(request, optTextDelay, optNumRequests, optTextForEach)
    })
    GridPane.setHalignment(buttonStart, HPos.RIGHT)
    grid.add(buttonStart, 1, 5)


    tab
  }

}




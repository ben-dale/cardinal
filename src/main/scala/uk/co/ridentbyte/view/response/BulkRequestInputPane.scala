package uk.co.ridentbyte.view.response

import javafx.geometry.{HPos, Insets}
import javafx.scene.control.{Button, Label, TextField}
import javafx.scene.layout.{GridPane, Priority}
import uk.co.ridentbyte.model.{CardinalRequest, Config}
import uk.co.ridentbyte.view.util.ColumnConstraintsBuilder

case class BulkRequestInputPane(getConfig: java.util.function.Function[Void, Config],
                                exportToBash: (List[CardinalRequest], Option[Long]) => Unit,
                                startBulkRequest: (CardinalRequest, Option[Long], Option[Int], Option[List[String]]) => Unit,
                                showErrorDialogCallback: java.util.function.Function[String, Void],
                                request: CardinalRequest) extends GridPane {

  setHgap(10)
  setVgap(10)
  setPadding(new Insets(20))
  getStyleClass.addAll("plain-border", "round-border")

  getColumnConstraints.add(new ColumnConstraintsBuilder().withPercentageWidth(40).withHgrow(Priority.ALWAYS).build)
  getColumnConstraints.add(new ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build)
  getColumnConstraints.add(new ColumnConstraintsBuilder().withHgrow(Priority.NEVER).build)

  private val labelHeader = new Label("Bulk Request")
  labelHeader.getStyleClass.addAll("header")
  GridPane.setColumnSpan(labelHeader, 3)
  GridPane.setHalignment(labelHeader, HPos.CENTER)
  add(labelHeader, 0, 0)

  private val labelDelay = new Label("Delay per request (ms)")
  GridPane.setHalignment(labelDelay, HPos.RIGHT)
  add(labelDelay, 0, 1)

  private val textDelay = new TextField("200")
  GridPane.setColumnSpan(textDelay, 2)
  add(textDelay, 1, 1)

  private val labelNumOfRequests = new Label("No. of requests")
  GridPane.setHalignment(labelNumOfRequests, HPos.RIGHT)
  add(labelNumOfRequests, 0, 2)

  private val textNumOfRequests = new TextField()
  GridPane.setColumnSpan(textNumOfRequests, 2)
  add(textNumOfRequests, 1, 2)

  private val labelOr = new Label("- OR -")
  GridPane.setColumnSpan(labelOr, 3)
  GridPane.setHalignment(labelOr, HPos.CENTER)
  add(labelOr, 0, 3)

  private val labelForEach = new Label("For each")
  GridPane.setHalignment(labelForEach, HPos.RIGHT)
  add(labelForEach, 0, 4)

  private val textForEach = new TextField()
  GridPane.setColumnSpan(textForEach, 2)
  textForEach.setPromptText("325, 454, 432 or 12..54")
  add(textForEach, 1, 4)

  private val buttonStart = new Button("Start")
  GridPane.setHalignment(buttonStart, HPos.RIGHT)
  buttonStart.setOnAction(_ => {
    startBulkRequest(request, getThrottle, getNumberOfRequests, getForEach)
  })
  add(buttonStart, 2, 5)

  private val buttonExportAsScript = new Button("Export as script...")
  GridPane.setHalignment(buttonExportAsScript, HPos.RIGHT)
  buttonExportAsScript.setOnAction(_ => asScript())
  add(buttonExportAsScript,1, 5)

  def getThrottle: Option[Long] = {
    if (textDelay.getText.trim.length == 0) {
      None
    } else {
      try {
        Some(textDelay.getText.trim.toLong)
      } catch {
        case _: Exception => None
      }
    }
  }

  def getNumberOfRequests: Option[Int] = {
    if (textNumOfRequests.getText.trim.length == 0) {
      None
    } else {
      try {
        Some(textNumOfRequests.getText.trim.toInt)
      } catch {
        case _: Exception => None
      }
    }
  }

  def getForEach: Option[List[String]] = {
    if (textForEach.getText.trim.length == 0) {
      None
    } else {
      val matcher = "([0-9]+)..([0-9]+)".r
      textForEach.getText.trim match {
        case matcher(a, b) => Some(a.toInt.to(b.toInt).map(_.toString).toList)
        case v => Some(v.split(",").toList)
      }
    }
  }

  def asScript(): Unit = {
    val requestCount = getNumberOfRequests
    val ids = getForEach
    if (requestCount.isDefined) {
      val requests = 0 until requestCount.get map { i =>
        request.withId(i.toString).processConstants(getConfig.apply(null))
      }
      exportToBash(requests.toList, getThrottle)
    } else if (ids.isDefined) {
      val requests = ids.get.zipWithIndex.map { case (id, _) =>
        request.withId(id).processConstants(getConfig.apply(null))
      }
      exportToBash(requests, getThrottle)
    } else {
      showErrorDialogCallback.apply("Invalid input. \nPlease provide a throttle and either a request count or a range value.")
    }
  }

}

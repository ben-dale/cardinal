package uk.co.ridentbyte.view.response

import javafx.geometry.{HPos, Insets}
import javafx.scene.control.{Button, Label, TextField}
import javafx.scene.layout.{GridPane, Priority}
import uk.co.ridentbyte.model.{CardinalBulkRequest, CardinalRequest, Config}
import uk.co.ridentbyte.view.util.ColumnConstraintsBuilder
import scala.collection.JavaConverters._

case class BulkRequestInputPane(getConfig: java.util.function.Function[Void, Config],
                                exportToBash: java.util.function.BiFunction[List[CardinalRequest], Int, Void],
                                startBulkRequest: CardinalBulkRequest => Unit,
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
    startBulkRequest(new CardinalBulkRequest(request, getThrottle, getNumberOfRequests, getForEach))
  })
  add(buttonStart, 2, 5)

  private val buttonExportAsScript = new Button("Export as script...")
  GridPane.setHalignment(buttonExportAsScript, HPos.RIGHT)
  buttonExportAsScript.setOnAction(_ => asScript())
  add(buttonExportAsScript,1, 5)

  def getThrottle: Int = {
    try {
      textDelay.getText.trim.toInt
    } catch {
      case _: Exception => 0
    }
  }

  def getNumberOfRequests: Int = {
    try {
      textNumOfRequests.getText.trim.toInt
    } catch {
      case _: Exception => 0
    }
  }

  def getForEach: java.util.List[String] = {
    val matcher = "([0-9]+)..([0-9]+)".r
    textForEach.getText.trim match {
      case matcher(a, b) => a.toInt.to(b.toInt).map(_.toString).toList.asJava
      case v => v.split(",").toList.asJava
    }
  }

  def asScript(): Unit = {
    val requestCount = getNumberOfRequests
    val ids = getForEach
    if (requestCount != 0) {
      val requests = 0 until requestCount map { i =>
        request.withId(i.toString).processConstants(getConfig.apply(null))
      }
      exportToBash.apply(requests.toList, getThrottle)
    } else if (!ids.isEmpty) {
      val requests = ids.asScala.zipWithIndex.map { case (id, _) =>
        request.withId(id).processConstants(getConfig.apply(null))
      }
      exportToBash.apply(requests.toList, getThrottle)
    } else {
      showErrorDialogCallback.apply("Invalid input. \nPlease provide a throttle and either a request count or a range value.")
    }
  }

}

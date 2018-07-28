package uk.co.ridentbyte.view.response

import javafx.geometry.{HPos, Insets}
import javafx.scene.control.{Button, Label, TextField}
import javafx.scene.layout.{GridPane, Priority}
import uk.co.ridentbyte.model.{CardinalRequest, Config}

case class BulkRequestInputPane(getConfig: () => Config,
                                exportToBash: (List[CardinalRequest], Option[Long]) => Unit,
                                startBulkRequest: (CardinalRequest, Option[Long], Option[Int], Option[List[String]]) => Unit,
                                request: CardinalRequest) extends GridPane {

  setHgap(10)
  setVgap(10)
  setPadding(new Insets(10))
  getStyleClass.addAll("plain-border", "round-border")

  private val labelDelay = new Label("Delay per request (ms)")
  GridPane.setHalignment(labelDelay, HPos.RIGHT)
  add(labelDelay, 0, 0)

  private val textDelay = new TextField("200")
  GridPane.setHgrow(textDelay, Priority.ALWAYS)
  add(textDelay, 1, 0)

  private val labelNumOfRequests = new Label("No. of requests")
  GridPane.setHalignment(labelNumOfRequests, HPos.RIGHT)
  add(labelNumOfRequests, 0, 1)

  private val textNumOfRequests = new TextField()
  GridPane.setHgrow(textNumOfRequests, Priority.ALWAYS)
  add(textNumOfRequests, 1, 1)

  private val labelOr = new Label("- OR -")
  GridPane.setColumnSpan(labelOr, 3)
  GridPane.setHalignment(labelOr, HPos.CENTER)
  add(labelOr, 0, 2)

  private val labelForEach = new Label("For each")
  GridPane.setHalignment(labelForEach, HPos.RIGHT)
  add(labelForEach, 0, 3)

  private val textForEach = new TextField()
  GridPane.setHgrow(textForEach, Priority.ALWAYS)
  textForEach.setPromptText("325, 454, 432 or 12..54")
  add(textForEach, 1, 3)

  private val buttonStart = new Button("Start")
  GridPane.setHgrow(textForEach, Priority.NEVER)
  buttonStart.setOnAction(_ => {
    startBulkRequest(request, getThrottle, getNumberOfRequests, getForEach)
  })
  add(buttonStart,1, 4)

  private val buttonExportAsScript = new Button("Export as script...")
  GridPane.setHgrow(textForEach, Priority.NEVER)
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
        request.withId(i.toString).processConstants(getConfig())
      }
      exportToBash(requests.toList, getThrottle)
    } else if (ids.isDefined) {
      val requests = ids.get.zipWithIndex.map { case (id, i) =>
        request.withId(id).processConstants(getConfig())
      }
      exportToBash(requests, getThrottle)
    }
  }

}

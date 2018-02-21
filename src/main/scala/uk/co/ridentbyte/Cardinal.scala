package uk.co.ridentbyte

import java.net._
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.geometry.{HPos, Insets, VPos}
import javafx.scene.Scene
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.layout.{ColumnConstraints, GridPane, Priority}
import javafx.stage.Stage

import scalaj.http.{Http, HttpOptions, HttpResponse}

object Cardinal {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Cardinal], args: _*)
  }
}

class Cardinal extends Application {

  val uriInput = new TextField()
  uriInput.setPromptText("http://localhost:8080")
  uriInput.setText("https://google.com")

  val tabPane = new TabPane()

  val bodyTextArea = new TextArea()
  bodyTextArea.setEditable(false)

  val headersListView = new ListView[String]()

  val verbSelect = new ChoiceBox[String](FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE", "HEAD", "CONNECT", "OPTIONS", "TRACE", "PATCH"))
  verbSelect.getSelectionModel.selectFirst()

  override def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("Cardinal")
    primaryStage.setWidth(750)
    primaryStage.setHeight(500)
    primaryStage.setMinHeight(500)
    primaryStage.setMinWidth(500)

    val gridPane = new GridPane()
    //    gridPane.setGridLinesVisible(true)
    gridPane.setHgap(10)
    gridPane.setVgap(10)
    gridPane.setPadding(new Insets(10, 10, 10, 10))

    val c1 = new ColumnConstraints()
    c1.setHgrow(Priority.ALWAYS)

    val c2 = new ColumnConstraints()
    gridPane.getColumnConstraints.addAll(c1, c2)

    gridPane.add(uriInput, 0, 0)


    gridPane.add(verbSelect, 1, 0)

    val sendRequestButton = new Button("Send Request")
    sendRequestButton.setOnAction(sendRequestAction)
    GridPane.setValignment(sendRequestButton, VPos.TOP)
    gridPane.add(sendRequestButton, 1, 1)
    //    gridPane.add(listView, 0, 2)
    gridPane.add(tabPane, 0, 1)

    val bodyTab = new Tab("Body")
    bodyTab.setClosable(false)
    bodyTab.setContent(bodyTextArea)
    tabPane.getTabs.add(bodyTab)

    val headersTab = new Tab("Headers")
    headersTab.setClosable(false)
    headersTab.setContent(headersListView)
    tabPane.getTabs.add(headersTab)

    val scene = new Scene(gridPane)
    primaryStage.setScene(scene)

    primaryStage.show()
  }

  def sendRequest(uri: String, verb: String): HttpResponse[String] = {
    println("SENDING...")
    Http(uri).option(HttpOptions.followRedirects(true)).method(verb).asString
  }

  def parseURI(rawUri: String): (String, Boolean) = {
    try {
      val rawUriWithProtocol = try {
        new URL(rawUri)
        new URI(rawUri)
        rawUri
      } catch {
        case _: URISyntaxException | _: MalformedURLException => "http://" + rawUri
      }
      val decodedURL = URLDecoder.decode(rawUriWithProtocol, "UTF-8")
      val url = new URL(decodedURL)
      val uri = new URI(url.getProtocol, url.getUserInfo, url.getHost, url.getPort, url.getPath, url.getQuery, url.getRef)
      (uri.toASCIIString, false)
    } catch {
      case _: Exception => (rawUri, true)
    }
  }

  def showErrorDialog(errorMessage: String): Unit = {
    val alert = new Alert(AlertType.ERROR)
    alert.setContentText(errorMessage)
    alert.showAndWait
  }

  case class Header(key: String, value: String) {
    override def toString: String = s"""$key: $value"""
  }

  def sendRequestAction(actionEvent: ActionEvent): Unit = {
    val verb = verbSelect.getSelectionModel.getSelectedItem
    val (uri, hasError) = parseURI(uriInput.getText.trim)
    if (hasError) {
      showErrorDialog("Invalid URL")
    } else {
      try {
        val response = sendRequest(uri, verb)
        val headers = response.headers.map({ case (k, v) => Header(k, v.head) })
        headersListView.getItems.clear()
        headers.foreach { header => headersListView.getItems.add(header.toString) }
        bodyTextArea.setText(response.body)
      } catch {
        case _: UnknownHostException => showErrorDialog("Unknown Host")
      }
    }
  }

}
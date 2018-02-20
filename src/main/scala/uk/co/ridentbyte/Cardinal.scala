package uk.co.ridentbyte

import java.net._
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, Button, ChoiceBox, TextField}
import javafx.scene.layout.{ColumnConstraints, GridPane, Priority}
import javafx.stage.Stage

import scalaj.http.{Http, HttpOptions}

object Cardinal {
  Application.launch()
}

class Cardinal extends Application {
  override def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("Cardinal")
    primaryStage.setWidth(750)
    primaryStage.setHeight(500)
    primaryStage.setMinHeight(500)
    primaryStage.setMinWidth(500)

    val gridPane = new GridPane()
    gridPane.setGridLinesVisible(true)
    gridPane.setHgap(10)
    gridPane.setVgap(10)
    gridPane.setPadding(new Insets(10, 10, 10, 10))

    val c1 = new ColumnConstraints()
    c1.setHgrow(Priority.ALWAYS)

    val c2 = new ColumnConstraints()
    gridPane.getColumnConstraints.addAll(c1, c2)

    val uriInput = new TextField()
    uriInput.setPromptText("http://localhost:8080")
    uriInput.setText("https://google.com")
    gridPane.add(uriInput, 0, 0)

    val verbSelect = new ChoiceBox[String](FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE", "HEAD"))
    verbSelect.getSelectionModel.selectFirst()
    gridPane.add(verbSelect, 1, 0)

    val sendRequestButton = new Button("Send Request")
    sendRequestButton.setOnAction((_: ActionEvent) => {
      val verb = verbSelect.getSelectionModel.getSelectedItem
      val (uri, hasError) = parseURI(uriInput.getText.trim)
      if (hasError) {
        showErrorDialog("Invalid URL")
      } else {
        try {
          println(Http(uri).option(HttpOptions.followRedirects(true)).asString)
        } catch {
          case _: UnknownHostException => showErrorDialog("Unknown Host")
        }
      }
    })

    gridPane.add(sendRequestButton, 1, 1)

    val scene = new Scene(gridPane)
    primaryStage.setScene(scene)

    primaryStage.show()
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
}
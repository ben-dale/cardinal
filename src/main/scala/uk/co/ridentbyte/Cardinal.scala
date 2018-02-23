package uk.co.ridentbyte

import java.net.{ConnectException, URISyntaxException, UnknownHostException}
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.layout._
import javafx.stage.Stage
import javax.net.ssl.SSLHandshakeException

import uk.co.ridentbyte.model.Header
import uk.co.ridentbyte.util.HttpUtil
import uk.co.ridentbyte.view.util.GridConstraints
import uk.co.ridentbyte.view.request.RequestPane
import uk.co.ridentbyte.view.response.ResponsePane

object Cardinal {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Cardinal], args: _*)
  }
}

class Cardinal extends Application {

  val rootGridPane = new GridPane()
  rootGridPane.getColumnConstraints.add(GridConstraints.widthColumnConstraint(30))
  rootGridPane.getColumnConstraints.add(GridConstraints.widthColumnConstraint(70))
  rootGridPane.getRowConstraints.add(GridConstraints.maxHeightRowConstraint)

  val requestPane = new RequestPane(sendRequest)
  val responsePane = new ResponsePane()

  override def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("Cardinal")

    rootGridPane.add(requestPane, 0, 0)
    rootGridPane.add(responsePane, 1, 0)

    val scene = new Scene(rootGridPane, 800, 400)
    primaryStage.setScene(scene)
    primaryStage.show()
  }
  def showErrorDialog(errorMessage: String): Unit = {
    val alert = new Alert(AlertType.ERROR)
    alert.setContentText(errorMessage)
    alert.showAndWait
  }

  def sendRequest(verb: String, uri: String): Unit = {
    try {
      val parsedUri = HttpUtil.parseURI(uri)
      val response = HttpUtil.sendRequest(parsedUri, verb)
      val headers = response.headers.map({ case (k, v) => Header(k, v.head) })
      responsePane.loadResponse(response.code, headers, response.body)
    } catch {
      case _: ConnectException => showErrorDialog("Connection refused")
      case _: URISyntaxException => showErrorDialog("Invalid URL")
      case _: UnknownHostException => showErrorDialog("Unknown Host")
      case _: SSLHandshakeException => showErrorDialog("SSL Handshake failed. Remote host closed connection during handshake.")
    }
  }

}
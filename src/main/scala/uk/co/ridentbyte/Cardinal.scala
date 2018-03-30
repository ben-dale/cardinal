package uk.co.ridentbyte

import java.io.File
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import uk.co.ridentbyte.model.{HttpResponseWrapper, Request}
import uk.co.ridentbyte.util.{HttpUtil, IOUtil}
import uk.co.ridentbyte.view.CardinalView

object Cardinal {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Cardinal], args: _*)
  }
}

class Cardinal extends Application {

  private var currentFile: Option[File] = None
  private val fileDir = "cardinal_files"
  private val httpUtil = new HttpUtil
  private val cardinalView = new CardinalView(clearAll, sendRequest)

  override def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("Cardinal")
    primaryStage.setMinHeight(400)
    primaryStage.setMinWidth(800)

    val scene = new Scene(cardinalView, 1000, 500)
    primaryStage.setScene(scene)
    primaryStage.show()
  }

  private def sendRequest(request: Request): HttpResponseWrapper = {
    val startTime = System.currentTimeMillis()
    val response = httpUtil.sendRequest(request)
    val totalTime = System.currentTimeMillis() - startTime
    HttpResponseWrapper(response, totalTime)
  }

  private def loadFileAsRequest(filename: String): Option[Request] = {
    try {
      currentFile = Some(IOUtil.loadFile(fileDir + "/" + filename + ".json"))
      val data = IOUtil.readFileContents(currentFile.get)
      Some(Request(data))
    } catch {
      case _: Exception => None
    }
  }

  private def saveChangesToCurrentFile(): Unit = {
    if (currentFile.isDefined) {
      val request = cardinalView.getRequest
      IOUtil.writeToFile(currentFile.get.getPath, request.toJson)
    }
  }
//
//  private def save(request: Request, filename: Option[String] = None): Unit = {
//    if (filename.isDefined) {
//      IOUtil.writeToFile(fileDir + "/" + filename.get + ".json", request.toJson)
//      cardinalView.addFile(filename.get)
//    } else {
//      val result = cardinalView.showInputDialog()
//      if (result.isDefined) {
//        IOUtil.writeToFile(fileDir + "/" + result.get + ".json", request.toJson)
//        cardinalView.addFile(result.get)
//      }
//    }
//  }

  private def clearAll(): Unit = {
    currentFile = None
    cardinalView.clearRequestResponsePanes()
  }
}
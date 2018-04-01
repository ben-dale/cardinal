package uk.co.ridentbyte

import java.io.File

import javafx.application.{Application, Platform}
import javafx.scene.Scene
import javafx.scene.control.{Alert, ButtonType}
import javafx.scene.control.Alert.AlertType
import javafx.scene.input.KeyCode
import javafx.stage.{FileChooser, Stage}
import uk.co.ridentbyte.model.{Config, HttpResponseWrapper, Request}
import uk.co.ridentbyte.util.{HttpUtil, IOUtil}
import uk.co.ridentbyte.view.CardinalView

object Cardinal {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Cardinal], args: _*)
  }
}

class Cardinal extends Application {

  private var currentConfig: Config = _
  private var currentFile: Option[File] = None
  private var currentStage: Stage = _
  private var unsavedChangesMade: Boolean = false
  private val httpUtil = new HttpUtil
  private val cardinalView = new CardinalView(() => getConfig, clearAllWithSavePrompt, saveChangesToCurrentFile, setCurrentFile, open, saveAs, sendRequest, setEnvironmentVariables, triggerUnsavedChangesMade)

  override def start(primaryStage: Stage): Unit = {
    currentStage = primaryStage
    primaryStage.setTitle("Cardinal")
    primaryStage.setMinHeight(600)
    primaryStage.setMinWidth(800)

    val scene = new Scene(cardinalView, 1000, 500)
    scene.getStylesheets.add(getClass.getClassLoader.getResource("style.css").toExternalForm)

    loadConfig()

    // Temporary action for dev
//    scene.setOnKeyPressed((k) => {
//      if (k.getCode == KeyCode.R) {
//        if (currentStage != null) {
//          currentStage.getScene.getStylesheets.clear()
//          println("[" + System.currentTimeMillis() + "] Reloading CSS")
//          val f = new File("src/main/resources/style.css")
//          currentStage.getScene.getStylesheets.add("file://" + f.getAbsolutePath)
//        }
//      }
//    })

    primaryStage.setOnCloseRequest((_) => {
      if (unsavedChangesMade) {
        if (currentFile.isDefined) {
          showConfirmDialog("Save changes to " + currentFile.get.getName + "?", () => saveChangesToCurrentFile(cardinalView.getRequest), () => Unit)
        } else {
          showConfirmDialog("Save unsaved changes?", () => saveAs(cardinalView.getRequest), () => Unit)
        }
      }
    })

    primaryStage.setScene(scene)
    primaryStage.show()
  }

  private def getConfig: Config = currentConfig

  private def setEnvironmentVariables(vars: List[String]): Unit = {
    currentConfig = currentConfig.withEnvironmentVariables(vars)
    saveChangesToConfig(currentConfig)
  }

  private def triggerUnsavedChangesMade(): Unit = {
    this.unsavedChangesMade = true
    if (currentFile.isDefined) {
      currentStage.setTitle(currentFile.get.getAbsolutePath + " *")
    }
  }

  private def sendRequest(request: Request): HttpResponseWrapper = {
    val startTime = System.currentTimeMillis()
    val response = httpUtil.sendRequest(request)
    val totalTime = System.currentTimeMillis() - startTime
    HttpResponseWrapper(response, totalTime)
  }

  private def saveChangesToCurrentFile(request: Request): Unit = {
    if (currentFile.isDefined) {
      IOUtil.writeToFile(currentFile.get, request.toJson)
      currentStage.setTitle(currentFile.get.getAbsolutePath)
      unsavedChangesMade = false
    }
  }

  private def loadConfig(): Unit = {
    val file = new File(System.getProperty("user.home") + "/.cardinal_config.json")
    if (file.exists && !file.isDirectory) {
      val lines = scala.io.Source.fromFile(file).getLines().mkString
      currentConfig = Config(lines)
    } else {
      // Create new empty config file if not present
      saveChangesToConfig(Config(List.empty[String]))
    }
  }

  private def saveChangesToConfig(config: Config): Unit = {
    val configFile = new File(System.getProperty("user.home") + "/.cardinal_config.json")
    IOUtil.writeToFile(configFile, config.toJson)
  }

  private def clearAllWithSavePrompt(): Unit = {
    if (unsavedChangesMade) {
      if (currentFile.isDefined) {
        showConfirmDialog("Save changes to " + currentFile.get.getName + "?", () => saveChangesToCurrentFile(cardinalView.getRequest), () => Unit)
      } else {
        showConfirmDialog("Save unsaved changes?", () => saveAs(cardinalView.getRequest), () => Unit)
      }
    }
    clearAll()
  }

  private def clearAll(): Unit = {
    currentFile = None
    unsavedChangesMade = false
    currentStage.setTitle("Cardinal")
    cardinalView.clearAll()
  }

  private def setCurrentFile(file: File): Unit = {
    currentFile = Option(file)
    if (currentFile.isDefined) {
      currentStage.setTitle(file.getAbsolutePath)
    }
  }

  private def open(): Unit = {
    val fileChooser = new FileChooser
    val selectedFile = fileChooser.showOpenDialog(currentStage)
    if (selectedFile != null) {
      if (unsavedChangesMade) {
        if (currentFile.isDefined) {
          showConfirmDialog("Save changes to " + currentFile.get.getName + "?", () => saveChangesToCurrentFile(cardinalView.getRequest), () => Unit)
        } else {
          showConfirmDialog("Save unsaved changes?", () => saveAs(cardinalView.getRequest), () => Unit)
        }
      }
      val lines = scala.io.Source.fromFile(selectedFile).getLines().mkString
      clearAll()
      cardinalView.loadRequest(Request(lines))
      cardinalView.setSaveDisabled(false)
      setCurrentFile(selectedFile)
      unsavedChangesMade = false
    }
  }

  private def saveAs(request: Request): Unit = {
    val fileChooser = new FileChooser
    val file = fileChooser.showSaveDialog(currentStage)
    if (file != null) {
      val fileWithExtension = if (!file.getAbsolutePath.endsWith(".json")) {
          new File(file.getAbsolutePath + ".json")
        } else {
          file
        }
      IOUtil.writeToFile(fileWithExtension, request.toJson)
      setCurrentFile(fileWithExtension)
      cardinalView.setSaveDisabled(false)
      unsavedChangesMade = false
    }
  }

  private def showConfirmDialog(message: String, onYesCallback:() => Unit, onNoCallback:() => Unit): Unit = {
    val alert = new Alert(AlertType.CONFIRMATION)
    alert.setTitle("Save Changes")
    alert.setContentText(message)
    alert.getButtonTypes.setAll(ButtonType.NO, ButtonType.YES)
    val result = alert.showAndWait()
    if (result.get == ButtonType.YES) {
      onYesCallback()
    } else {
      onNoCallback()
    }
  }


}
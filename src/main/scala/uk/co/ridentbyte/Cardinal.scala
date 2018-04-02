package uk.co.ridentbyte

import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control._
import javafx.scene.control.Alert.AlertType
import javafx.scene.layout.BorderPane
import javafx.stage.{FileChooser, Stage}
import uk.co.ridentbyte.model.{Config, HttpResponseWrapper, Request}
import uk.co.ridentbyte.util.{HttpUtil, IOUtil}
import uk.co.ridentbyte.view.{CardinalMenuBar, CardinalView}
import uk.co.ridentbyte.view.dialog.{BasicAuthInputDialog, EnvironmentVariablesEditDialog, FormUrlEncodedInputDialog}

object Cardinal {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Cardinal], args: _*)
  }
}

class Cardinal extends Application {

  private val configLocation: String = System.getProperty("user.home") + "/.cardinal_config.json"
  private var currentConfig: Config = _
  private var currentFile: Option[File] = None
  private var currentStage: Stage = _
  private var unsavedChangesMade: Boolean = false
  private val httpUtil = new HttpUtil
  private val cardinalView = new CardinalView(showErrorDialog, () => currentConfig, sendRequest, triggerUnsavedChangesMade)
  private val menuBar = new CardinalMenuBar(showAsCurl, open, saveChangesToCurrentFile,saveAs,clearAll, showEnvironmentVariablesInput, showFormUrlEncodedInput, showBasicAuthInput)

  override def start(primaryStage: Stage): Unit = {
    currentStage = primaryStage
    primaryStage.setTitle("Cardinal")
    primaryStage.setMinHeight(600)
    primaryStage.setMinWidth(800)

    val view = new BorderPane()
    view.setTop(menuBar)
    view.setCenter(cardinalView)

    val scene = new Scene(view, 1000, 500)
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
          showConfirmDialog("Save changes to " + currentFile.get.getName + "?", () => saveChangesToCurrentFile(), () => Unit)
        } else {
          showConfirmDialog("Save unsaved changes?", () => saveAs(), () => Unit)
        }
      }
    })

    primaryStage.setScene(scene)
    primaryStage.show()

  }

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

  private def saveChangesToCurrentFile(): Unit = {
    if (currentFile.isDefined) {
      IOUtil.writeToFile(currentFile.get, cardinalView.getRequest.toJson)
      currentStage.setTitle(currentFile.get.getAbsolutePath)
      unsavedChangesMade = false
    }
  }

  private def loadConfig(): Unit = {
    val file = new File(configLocation)
    if (file.exists && !file.isDirectory) {
      val lines = scala.io.Source.fromFile(file).getLines().mkString
      currentConfig = Config(lines)
    } else {
      // Create new empty config file if not present
      saveChangesToConfig(Config(List.empty[String]))
    }
  }

  private def saveChangesToConfig(config: Config): Unit = {
    val configFile = new File(configLocation)
    IOUtil.writeToFile(configFile, config.toJson)
  }

  private def clearAllWithSavePrompt(): Unit = {
    if (unsavedChangesMade) {
      if (currentFile.isDefined) {
        showConfirmDialog("Save changes to " + currentFile.get.getName + "?", () => saveChangesToCurrentFile(), () => Unit)
      } else {
        showConfirmDialog("Save unsaved changes?", () => saveAs(), () => Unit)
      }
    }
    clearAll()
  }

  private def clearAll(): Unit = {
    currentFile = None
    unsavedChangesMade = false
    menuBar.setSaveDisabled(true)
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
          showConfirmDialog("Save changes to " + currentFile.get.getName + "?", () => saveChangesToCurrentFile(), () => Unit)
        } else {
          showConfirmDialog("Save unsaved changes?", () => saveAs(), () => Unit)
        }
      }
      val lines = scala.io.Source.fromFile(selectedFile).getLines().mkString
      clearAll()
      cardinalView.loadRequest(Request(lines))
      menuBar.setSaveDisabled(false)
      setCurrentFile(selectedFile)
      unsavedChangesMade = false
    }
  }

  private def saveAs(): Unit = {
    val fileChooser = new FileChooser
    val file = fileChooser.showSaveDialog(currentStage)
    if (file != null) {
      val fileWithExtension = if (!file.getAbsolutePath.endsWith(".json")) {
          new File(file.getAbsolutePath + ".json")
        } else {
          file
        }
      IOUtil.writeToFile(fileWithExtension, cardinalView.getRequest.toJson)
      setCurrentFile(fileWithExtension)
      menuBar.setSaveDisabled(false)
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

  def showBasicAuthInput(): Unit = {
    val dialog = new BasicAuthInputDialog
    val results = dialog.showAndWait()
    if (results.isPresent) {
      val username = results.get.username
      val password = results.get.password
      val encoded = Base64.getEncoder.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8))
      cardinalView.addHeader("Authorization: Basic " + encoded)
    }
  }

  def showFormUrlEncodedInput(): Unit = {
    val dialog = new FormUrlEncodedInputDialog(cardinalView.getRequest.body.getOrElse(""))
    val results = dialog.showAndWait()
    if (results.isPresent) {
      cardinalView.setBody(results.get.toBodyString)
      cardinalView.addHeader("Content-Type: application/x-www-form-urlencoded")
    }
  }

  def showEnvironmentVariablesInput(): Unit = {
    val dialog = new EnvironmentVariablesEditDialog(currentConfig.environmentVariables)
    val results = dialog.showAndWait()
    if (results.isPresent) {
      setEnvironmentVariables(results.get)
    }
  }

  def showErrorDialog(errorMessage: String): Unit = {
    val alert = new Alert(AlertType.ERROR)
    alert.setContentText(errorMessage)
    alert.showAndWait
  }

  def showAsCurl(): Unit = {
    val request = cardinalView.getRequest
    if (request.uri.trim.length == 0) {
      showErrorDialog("Please enter a URL.")
    } else {
      cardinalView.loadCurlCommand(request.toCurl(currentConfig))
    }
  }

}
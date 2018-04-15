package uk.co.ridentbyte

import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control._
import javafx.scene.control.Alert.AlertType
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.stage.{FileChooser, Stage}
import javafx.scene.text.Font
import uk.co.ridentbyte.model.{Config, HttpResponseWrapper, Request}
import uk.co.ridentbyte.util.{HttpUtil, IOUtil}
import uk.co.ridentbyte.view.{CardinalInfoTab, CardinalMenuBar, CardinalView}
import uk.co.ridentbyte.view.dialog.{BasicAuthInputDialog, EnvironmentVariablesEditDialog, FormUrlEncodedInputDialog}

import scala.collection.JavaConverters._

object Cardinal {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Cardinal], args: _*)
  }
}

class Cardinal extends Application {

  private val configLocation: String = System.getProperty("user.home") + "/.cardinal_config.json"
  private var currentConfig: Config = _
  private var currentStage: Stage = _
  private val httpUtil = new HttpUtil
  private val menuBar = new CardinalMenuBar(newTab, showAsCurl, open, saveChangesToCurrentFile, saveAs, clearAll, showEnvironmentVariablesInput, showFormUrlEncodedInput, showBasicAuthInput)
  private val cardinalTabs = new TabPane()

  override def start(primaryStage: Stage): Unit = {
    currentStage = primaryStage
    primaryStage.setTitle("Cardinal")
    primaryStage.setMinHeight(600)
    primaryStage.setMinWidth(800)

    val view = new BorderPane()
    view.setTop(menuBar)
    view.setCenter(cardinalTabs)

    val scene = new Scene(view, 1000, 500)
    scene.getStylesheets.add(getClass.getClassLoader.getResource("style.css").toExternalForm)

    val font = Font.loadFont(getClass.getClassLoader.getResource("OpenSans-Regular.ttf").toExternalForm, 13)

    loadConfig()

    // Temporary action for dev
    scene.setOnKeyPressed((k) => {
      if (k.getCode == KeyCode.R) {
        if (currentStage != null) {
          currentStage.getScene.getStylesheets.clear()
          println("[" + System.currentTimeMillis() + "] Reloading CSS")
          val f = new File("src/main/resources/style.css")
          currentStage.getScene.getStylesheets.add("file://" + f.getAbsolutePath)
        }
      }
    })

    primaryStage.setOnCloseRequest((e) => {
      val allTabs = cardinalTabs.getTabs.asScala.filter(_.isInstanceOf[CardinalTab]).map(_.asInstanceOf[CardinalTab])
      val unsavedTabs = allTabs.count(_.hasUnsavedChanges)
      if (unsavedTabs > 0) {
        showConfirmDialog("Save unsaved changes?", saveAllUnsavedChanges, () => Unit)
      }
    })

    primaryStage.setScene(scene)
    primaryStage.show()

    cardinalTabs.getTabs.add(CardinalInfoTab())
    newTab()

  }

  private def setEnvironmentVariables(vars: List[String]): Unit = {
    currentConfig = currentConfig.withEnvironmentVariables(vars)
    saveChangesToConfig(currentConfig)
  }

  private def triggerUnsavedChangesMade(): Unit = {
    getCurrentTab.handleUnsavedChangesMade()
  }

  private def sendRequest(request: Request): HttpResponseWrapper = {
    val startTime = System.currentTimeMillis()
    val response = httpUtil.sendRequest(request)
    val totalTime = System.currentTimeMillis() - startTime
    HttpResponseWrapper(response, totalTime)
  }

  private def saveChangesToCurrentFile(): Unit = {
    val currentTab = getCurrentTab
    if (currentTab != null && currentTab.currentFile.isDefined) {
      IOUtil.writeToFile(currentTab.currentFile.get, currentTab.content.getRequest.toJson)
      currentTab.setUnsavedChanges(false)
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

  private def clearAll(): Unit = {
    val currentTab = getCurrentTab
    if (currentTab != null) {
      currentTab.content.clearAll()
    }
  }

  private def open(): Unit = {
    val fileChooser = new FileChooser
    val selectedFile = fileChooser.showOpenDialog(currentStage)
    if (selectedFile != null) {
      val lines = scala.io.Source.fromFile(selectedFile).getLines().mkString
      val cardinalView = new CardinalView(showErrorDialog, () => currentConfig, sendRequest, triggerUnsavedChangesMade)
      cardinalTabs.getTabs.add(CardinalTab(Some(selectedFile), cardinalView))
      cardinalTabs.getSelectionModel.selectLast()
      cardinalView.loadRequest(Request(lines))
      getCurrentTab.setUnsavedChanges(false)
    }
  }

  private def newTab(): Unit = {
    cardinalTabs.getTabs.add(CardinalTab(None, new CardinalView(showErrorDialog, () => currentConfig, sendRequest, triggerUnsavedChangesMade)))
    cardinalTabs.getSelectionModel.selectLast()
  }

  private def saveAs(): Unit = {
    val currentTab = getCurrentTab
    if (currentTab != null) {
      val fileChooser = new FileChooser
      val file = fileChooser.showSaveDialog(currentStage)
      if (file != null) {
        val fileWithExtension = if (!file.getAbsolutePath.endsWith(".json")) {
          new File(file.getAbsolutePath + ".json")
        } else {
          file
        }
        IOUtil.writeToFile(fileWithExtension, currentTab.content.getRequest.toJson)
//        menuBar.setSaveDisabled(false)
        currentTab.setCurrentFile(fileWithExtension)
        currentTab.setUnsavedChanges(false)
      }
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
    val currentTab = getCurrentTab
    if (currentTab != null) {
      val dialog = new BasicAuthInputDialog
      val results = dialog.showAndWait()
      if (results.isPresent) {
        val username = results.get.username
        val password = results.get.password
        val encoded = Base64.getEncoder.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8))
        currentTab.content.addHeader("Authorization: Basic " + encoded)
      }
    }
  }

  def showFormUrlEncodedInput(): Unit = {
    val currentTab = getCurrentTab
    if (currentTab != null) {
      val dialog = new FormUrlEncodedInputDialog(currentTab.content.getRequest.body.getOrElse(""))
      val results = dialog.showAndWait()
      if (results.isPresent) {
        currentTab.content.setBody(results.get.toBodyString)
        currentTab.content.addHeader("Content-Type: application/x-www-form-urlencoded")
      }
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
    val currentTab = getCurrentTab
    if (currentTab != null) {
      val request = currentTab.content.getRequest
      if (request.uri.trim.length == 0) {
        showErrorDialog("Please enter a URL.")
      } else {
        currentTab.content.loadCurlCommand(request.toCurl(currentConfig))
      }
    }
  }

  private def getCurrentTab: CardinalTab = {
    cardinalTabs.getSelectionModel.getSelectedItem.asInstanceOf[CardinalTab]
  }

  def saveAllUnsavedChanges(): Unit = {
    cardinalTabs.getTabs.asScala.foreach {
      case t: CardinalTab =>
        val currentFile = t.currentFile
        if (currentFile.isDefined) {
          IOUtil.writeToFile(currentFile.get, t.content.getRequest.toJson)
        }
      case _ => // Currently does not save unsaved changes to "Untitled" files
    }
  }

  case class CardinalTab(var currentFile: Option[File], content: CardinalView)
    extends Tab(if (currentFile.isDefined) currentFile.get.getName else "Untitled", content) {
    private var unsavedChanges = false

    getStyleClass.add("cardinal-font")

    setOnCloseRequest((_) => {
      if (unsavedChanges) {
        showConfirmDialog("Save unsaved changes?", saveChangesToCurrentFile, () => Unit)
      }
    })

    def handleUnsavedChangesMade(): Unit = {
      if (!getText.endsWith("*")) {
        unsavedChanges = true
        setText(getText + "*")
      }
    }

    def setCurrentFile(currentFile: File): Unit = {
      this.currentFile = Some(currentFile)
      setText(currentFile.getName)
    }

    def hasUnsavedChanges: Boolean = unsavedChanges
    def setUnsavedChanges(unsavedChanges: Boolean): Unit = {
      if (!unsavedChanges) {
        currentFile match {
          case Some(f) => setText(f.getName)
          case _ => setText("Untitled")
        }
      }
      this.unsavedChanges = unsavedChanges
    }
  }

}
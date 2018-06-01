package uk.co.ridentbyte

import java.io.{File, FileWriter}

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control._
import javafx.scene.control.Alert.AlertType
import javafx.scene.layout.BorderPane
import javafx.stage.{FileChooser, Stage}
import javafx.scene.text.Font
import uk.co.ridentbyte.model._
import uk.co.ridentbyte.view.{CardinalInfoTab, CardinalMenuBar, CardinalView}
import uk.co.ridentbyte.view.dialog.{BasicAuthInputDialog, EnvironmentVariablesEditDialog, FormUrlEncodedInputDialog}

import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.Random

object Cardinal {
  val firstNames: Words = Words(Source.fromResource("firstNames.txt").getLines().toList, Random)
  val lastNames: Words = Words(Source.fromResource("firstNames.txt").getLines().toList, Random)
  val countries: Words = Words(Source.fromResource("countries.txt").getLines().toList, Random)
  val objects: Words = Words(Source.fromResource("objects.txt").getLines().toList, Random)
  val actions: Words = Words(Source.fromResource("actions.txt").getLines().toList, Random)
  val businessEntities: Words = Words(Source.fromResource("businessEntities.txt").getLines().toList, Random)
  val communications: Words = Words(Source.fromResource("communications.txt").getLines().toList, Random)
  val places: Words = Words(Source.fromResource("places.txt").getLines().toList, Random)

  val vocabulary = Vocabulary(firstNames, lastNames, places, objects, actions, countries, communications, businessEntities)

  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Cardinal], args: _*)
  }
}

class Cardinal extends Application {

  private val configLocation: String = System.getProperty("user.home") + "/.cardinal_config.json"
  private var currentConfig: Config = _
  private var currentStage: Stage = _
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

    Font.loadFont(getClass.getClassLoader.getResource("OpenSans-Regular.ttf").toExternalForm, 13)

    val file = new File(configLocation)
    if (file.exists && !file.isDirectory) {
      val lines = scala.io.Source.fromFile(file).getLines().mkString
      currentConfig = Config(lines)
    } else {
      // Create new empty config file if not present
      saveChangesToConfig(Config(List.empty[String]))
    }

    // Temporary action for dev
//    scene.setOnKeyPressed(k => {
//      if (k.getCode == KeyCode.R) {
//        if (currentStage != null) {
//          currentStage.getScene.getStylesheets.clear()
//          println("[" + System.currentTimeMillis() + "] Reloading CSS")
//          val f = new File("src/main/resources/style.css")
//          currentStage.getScene.getStylesheets.add("file://" + f.getAbsolutePath)
//        }
//      }
//    })

    primaryStage.setOnCloseRequest(r => {
      val allTabs = cardinalTabs.getTabs.asScala.filter(_.isInstanceOf[CardinalTab]).map(_.asInstanceOf[CardinalTab])
      val unsavedTabs = allTabs.count(_.hasUnsavedChanges)
      if (unsavedTabs > 0) {
        showConfirmDialog("Save all unsaved changes?", saveAllUnsavedChanges, () => Unit, () => r.consume())
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
    val response = Http(request).send
    val totalTime = System.currentTimeMillis() - startTime
    HttpResponseWrapper(response, totalTime)
  }

  private def saveChangesToCurrentFile(): Unit = {
    val currentTab = getCurrentTab
    if (currentTab != null && currentTab.currentFile.isDefined) {
      writeToFile(currentTab.currentFile.get, currentTab.content.getRequest.toJson)
      currentTab.setUnsavedChanges(false)
    } else {
      saveAs()
    }
  }

  private def saveChangesToConfig(config: Config): Unit = {
    val configFile = new File(configLocation)
    writeToFile(configFile, config.toJson)
  }

  private def clearAll(): Unit = {
    val currentTab = getCurrentTab
    if (currentTab != null) {
      currentTab.content.clearAll()
    }
  }

  private def open(): Unit = {
    val fileChooser = new FileChooser
    val selectedFiles = fileChooser.showOpenMultipleDialog(currentStage)
    if (selectedFiles != null) {
      selectedFiles.asScala.foreach { selectedFile =>
        val lines = scala.io.Source.fromFile(selectedFile).getLines().mkString
        val cardinalView = new CardinalView(showErrorDialog, () => currentConfig, sendRequest, triggerUnsavedChangesMade)
        cardinalTabs.getTabs.add(CardinalTab(Some(selectedFile), cardinalView))
        cardinalTabs.getSelectionModel.selectLast()
        cardinalView.loadRequest(Request(lines))
        getCurrentTab.setUnsavedChanges(false)
      }
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

        if (currentTab.currentFile.isEmpty) {
          // New file so just save file
          writeToFile(fileWithExtension, currentTab.content.getRequest.toJson)
          currentTab.setCurrentFile(fileWithExtension)
          currentTab.setUnsavedChanges(false)
        } else {
          // Existing file so save and open in new tab
          val request = currentTab.content.getRequest
          writeToFile(fileWithExtension, request.toJson)
          val cardinalView = new CardinalView(showErrorDialog, () => currentConfig, sendRequest, triggerUnsavedChangesMade)
          cardinalTabs.getTabs.add(CardinalTab(Some(fileWithExtension), cardinalView))
          cardinalTabs.getSelectionModel.selectLast()
          cardinalView.loadRequest(request)
          getCurrentTab.setUnsavedChanges(false)
        }
      }
    }
  }

  private def showConfirmDialog(message: String, onYesCallback:() => Unit, onNoCallback:() => Unit, onCancelCallback: () => Unit): Unit = {
    val alert = new Alert(AlertType.CONFIRMATION)
    alert.setTitle("Save Changes")
    alert.setContentText(message + "\n\n")

    val yesButton = new ButtonType("Yes", ButtonBar.ButtonData.RIGHT)
    val noButton = new ButtonType("No", ButtonBar.ButtonData.RIGHT)
    val cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.LEFT)
    alert.getButtonTypes.setAll(cancelButton, noButton, yesButton)

    val result = alert.showAndWait()
    if (result.get.getText == "Yes") {
      onYesCallback()
    } else if (result.get.getText == "No") {
      onNoCallback()
    } else if (result.get.getText == "Cancel") {
      onCancelCallback()
    }
  }

  def showBasicAuthInput(): Unit = {
    val currentTab = getCurrentTab
    if (currentTab != null) {
      val dialog = new BasicAuthInputDialog
      val result = dialog.showAndWait()
      if (result.isPresent) {
        currentTab.content.addHeader(result.get.asAuthHeader)
      }
    }
  }

  def showFormUrlEncodedInput(): Unit = {
    val currentTab = getCurrentTab
    if (currentTab != null) {
      val dialog = new FormUrlEncodedInputDialog(currentTab.content.getRequest.body.getOrElse(""))
      val result = dialog.showAndWait()
      if (result.isPresent) {
        currentTab.content.setBody(result.get.toString)
        currentTab.content.addHeader(result.get.header)
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
          writeToFile(currentFile.get, t.content.getRequest.toJson)
        }
      case _ => // Currently does not save unsaved changes to "Untitled" files
    }
  }

  case class CardinalTab(var currentFile: Option[File], content: CardinalView)
    extends Tab(if (currentFile.isDefined) currentFile.get.getName else "Untitled", content) {
    private var unsavedChanges = false

    getStyleClass.add("cardinal-font")

    setOnCloseRequest(r => {
      if (unsavedChanges) {
        showConfirmDialog("Save unsaved changes?", saveChangesToCurrentFile, () => Unit, () => r.consume())
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

  def writeToFile(file: File, data: String): Unit = {
    val fileWriter = new FileWriter(file)
    fileWriter.write(data)
    fileWriter.close()
  }

}
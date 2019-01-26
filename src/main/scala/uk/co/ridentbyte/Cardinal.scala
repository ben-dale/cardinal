package uk.co.ridentbyte

import java.io.{File, FileWriter}
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.util.function

import javafx.application.{Application, Platform}
import javafx.scene.Scene
import javafx.scene.control._
import javafx.scene.control.Alert.AlertType
import javafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination, KeyEvent}
import javafx.scene.layout.BorderPane
import javafx.stage.{FileChooser, Stage}
import javafx.scene.text.Font
import uk.co.ridentbyte.model._
import uk.co.ridentbyte.view.{CardinalMenuBar, CardinalView}
import uk.co.ridentbyte.view.dialog.{BasicAuthInputDialog, EnvironmentVariablesEditDialog, FormUrlEncodedInputDialog}

import scala.collection.JavaConverters._
import scala.io.Source
import scala.runtime.BoxedUnit

object Cardinal {
  val firstNames: Words = new Words(Source.fromResource("firstNames.txt").getLines().toList.asJava, new java.util.Random())
  val lastNames: Words = new Words(Source.fromResource("firstNames.txt").getLines().toList.asJava, new java.util.Random())
  val countries: Words = new Words(Source.fromResource("countries.txt").getLines().toList.asJava, new java.util.Random())
  val objects: Words = new Words(Source.fromResource("objects.txt").getLines().toList.asJava, new java.util.Random())
  val actions: Words = new Words(Source.fromResource("actions.txt").getLines().toList.asJava, new java.util.Random())
  val businessEntities: Words = new Words(Source.fromResource("businessEntities.txt").getLines().toList.asJava, new java.util.Random())
  val communications: Words = new Words(Source.fromResource("communications.txt").getLines().toList.asJava, new java.util.Random())
  val places: Words = new Words(Source.fromResource("places.txt").getLines().toList.asJava, new java.util.Random())
  val loremIpsum: Words = new Words(Source.fromResource("loremipsum.txt").getLines().toList.asJava, new java.util.Random())
  val emoji: Words = new Words(Source.fromResource("emoji.txt").getLines().toList.asJava, new java.util.Random())

  val vocabulary = Vocabulary(firstNames, lastNames, places, objects, actions, countries, communications, businessEntities, loremIpsum, emoji)

  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Cardinal], args: _*)
  }
}

class Cardinal extends Application {

  private val configLocation: String = System.getProperty("user.home") + "/.cardinal_config.json"
  private var currentConfig: Config = _
  private var currentStage: Stage = _
  private val menuBar = new CardinalMenuBar(newTab, open, save, saveAs, clearAll, showEnvironmentVariablesInput, showFormUrlEncodedInput, showBasicAuthInput)
  private val cardinalTabs = new TabPane()

  private val newRequestTab = new Tab("+")
  newRequestTab.setClosable(false)
  newRequestTab.setOnSelectionChanged(e => {
    if (e.getSource.asInstanceOf[Tab] == newRequestTab) {
      newTab()
    }
  })
  cardinalTabs.getTabs.add(newRequestTab)

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

    scene.addEventFilter(KeyEvent.KEY_PRESSED, (keyEvent: KeyEvent) => {
      val saveAsCombo = new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN)
      val saveCombo = new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN)
      val closeTabCombo = new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN)
      val openCombo = new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN)
      val newCombo = new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN)

      if (saveCombo.`match`(keyEvent)) {
        save(() => Unit)
      } else if (saveAsCombo.`match`(keyEvent)) {
        saveAs(() => Unit)
      } else if (closeTabCombo.`match`(keyEvent) && getCurrentTab != null) {
        val currentTab = getCurrentTab
        val remove: () => Unit = () => {
          cardinalTabs.getTabs.remove(currentTab)
          openNewFileIfNoneOpen()
        }
        if (currentTab.hasUnsavedChanges) {
          val saveCallback: () => Unit = () => {
            var shouldRemove = true
            save(() => shouldRemove = false)
            if (shouldRemove) {
              remove()
            }
          }
          showConfirmDialog("Save unsaved changes?", saveCallback, remove, () => Unit)
        } else {
          remove()
        }
      } else if (openCombo.`match`(keyEvent)) {
        open()
      } else if (newCombo.`match`(keyEvent)) {
        newTab()
      }
    })

    Font.loadFont(getClass.getClassLoader.getResource("OpenSans-Regular.ttf").toExternalForm, 13)

    val file = new File(configLocation)
    if (file.exists && !file.isDirectory) {
      val lines = scala.io.Source.fromFile(file).getLines().mkString
      currentConfig = Config(lines)
    } else {
      // Create new empty config file if not present
      val conf = Config(List.empty[EnvironmentVariable])
      saveChangesToConfig(conf)
      currentConfig = conf
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
  }

  private def setEnvironmentVariables(vars: List[EnvironmentVariable]): Unit = {
    currentConfig = currentConfig.withEnvironmentVariables(vars)
    saveChangesToConfig(currentConfig)
  }

  private def triggerUnsavedChangesMade: java.util.function.Function[Void, Void] = {
    new java.util.function.Function[Void, Void] {
      override def apply(t: Void): Void = {
        getCurrentTab.handleUnsavedChangesMade()
        null
      }
    }
  }

  private def sendRequest(request: CardinalRequest): CardinalResponse = {
    val startTime = System.currentTimeMillis()
    val response = Http(request).send
    val totalTime = System.currentTimeMillis() - startTime
    new CardinalResponse(response, totalTime)
  }

  private def save(onCancel: () => Unit): Unit = {
    val currentTab = getCurrentTab
    if (currentTab != null && currentTab.currentFile.isDefined) {
      writeToFile(currentTab.currentFile.get, currentTab.content.getRequest.toJson)
      currentTab.setUnsavedChanges(false)
    } else {
      saveAs(onCancel)
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
        val cardinalView = new CardinalView(showAsCurl, showErrorDialog, () => currentConfig, exportToCsv, exportToBash, sendRequest, triggerUnsavedChangesMade)
        addTab(CardinalTab(Some(selectedFile), cardinalView))
        cardinalView.loadRequest(CardinalRequest(lines))
        getCurrentTab.setUnsavedChanges(false)
      }
    }
  }

  private def newTab(): Unit = {
    cardinalTabs.getTabs.add(
      cardinalTabs.getTabs.size - 1,
      CardinalTab(None, new CardinalView(showAsCurl, showErrorDialog, () => currentConfig, exportToCsv, exportToBash, sendRequest, triggerUnsavedChangesMade))
    )
    cardinalTabs.getSelectionModel.select(cardinalTabs.getTabs.size() - 2)
  }

  private def saveAs(onCancel: () => Unit): Unit = {
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
          val cardinalView = new CardinalView(showAsCurl, showErrorDialog, () => currentConfig, exportToCsv, exportToBash, sendRequest, triggerUnsavedChangesMade)
          addTab(CardinalTab(Some(fileWithExtension), cardinalView))
          cardinalView.loadRequest(request)
          getCurrentTab.setUnsavedChanges(false)
        }
      } else {
        onCancel()
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
    val dialog = new EnvironmentVariablesEditDialog(currentConfig.getEnvironmentVariables)
    val results = dialog.showAndWait()
    if (results.isPresent) {
      setEnvironmentVariables(results.get)
    }
  }

  def showErrorDialog(errorMessage: String): BoxedUnit = {
    Platform.runLater(() => {
      val alert = new Alert(AlertType.ERROR)
      alert.setContentText(errorMessage)
      alert.showAndWait
    })
    scala.runtime.BoxedUnit.UNIT
  }

  def showAsCurl(): java.util.function.Function[Void, Void] = {
    new function.Function[Void, Void] {
      override def apply(t: Void): Void = {
        val currentTab = getCurrentTab
        if (currentTab != null) {
          val request = currentTab.content.getRequest
          if (request.uri.trim.length == 0) {
            showErrorDialog("Please enter a URL.")
          } else {
            currentTab.content.loadCurlCommand(request.toCurl(currentConfig))
          }
        }
        null
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
        showConfirmDialog("Save unsaved changes?", () => save(r.consume), () => Unit, () => r.consume())
      }
      Platform.runLater(() => openNewFileIfNoneOpen())
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

  def openNewFileIfNoneOpen(): Unit = {
    if (cardinalTabs.getTabs.size() == 0) {
      newTab()
    }
  }

  def exportToCsv(requestAndResponses: List[(CardinalRequest, Option[CardinalResponse])]): Unit = {
    val header = CardinalRequest.csvHeaders + "," + CardinalResponse.csvHeaders
    val content = requestAndResponses.map { case (req, res) =>
      req.toCSV + "," + res.getOrElse(BlankCardinalResponse()).toCSV
    }.mkString("\n")
    val fileChooser = new FileChooser
    val file = fileChooser.showSaveDialog(currentStage)
    if (file != null) {
      val fileWithExtension = if (!file.getAbsolutePath.endsWith(".csv")) {
        new File(file.getAbsolutePath + ".csv")
      } else {
        file
      }
      writeToFile(fileWithExtension, header + "\n" + content)
    }
  }

  def exportToBash(requests: List[CardinalRequest], throttle: Option[Long]): Unit = {
    val bashScript = if (throttle.isDefined) {
      new BashScript(requests.asJava, currentConfig, throttle.get)
    } else {
      new BashScript(requests.asJava, currentConfig)
    }
    val fileChooser = new FileChooser
    val file = fileChooser.showSaveDialog(currentStage)
    if (file != null) {
      writeToFile(file, bashScript.toString)
      val posix = Set(
        PosixFilePermission.OWNER_WRITE,
        PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_EXECUTE,
        PosixFilePermission.GROUP_READ,
        PosixFilePermission.GROUP_EXECUTE,
        PosixFilePermission.OTHERS_READ,
        PosixFilePermission.OTHERS_EXECUTE
      ).asJava
      Files.setPosixFilePermissions(file.toPath, posix)
    }
  }

  def addTab(tab: Tab): Unit = {
    cardinalTabs.getTabs.add(cardinalTabs.getTabs.size - 1, tab)
    cardinalTabs.getSelectionModel.select(cardinalTabs.getTabs.size() - 2)
  }

}
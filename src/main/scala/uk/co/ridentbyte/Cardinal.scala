package uk.co.ridentbyte

import java.io.{File, FileWriter}
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.util
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

  val vocabulary = new Vocabulary(firstNames, lastNames, places, objects, actions, countries, communications, businessEntities, loremIpsum, emoji)

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
      newTab.apply(null)
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
        save.apply(null)
      } else if (saveAsCombo.`match`(keyEvent)) {
        saveAs.apply(null)
      } else if (closeTabCombo.`match`(keyEvent) && getCurrentTab != null) {
        val currentTab = getCurrentTab
        val remove = new function.Function[Void, Void] {
          override def apply(t: Void): Void = {
            cardinalTabs.getTabs.remove(currentTab)
            openNewFileIfNoneOpen()
            null
          }
        }
        if (currentTab.hasUnsavedChanges) {
          val saveCallback = new function.Function[Void, Void] {
            override def apply(t: Void): Void = {
              save.apply(null)
              remove.apply(null)
              null
            }
          }

          val cancelCallback = new function.Function[Void, Void] {
            override def apply(t: Void): Void = {
              null
            }
          }


          showConfirmDialog.apply("Save unsaved changes?", saveCallback, remove, cancelCallback)
        } else {
          remove.apply(null)
        }
      } else if (openCombo.`match`(keyEvent)) {
        open.apply(null)
      } else if (newCombo.`match`(keyEvent)) {
        newTab.apply(null)
      }
    })

    Font.loadFont(getClass.getClassLoader.getResource("OpenSans-Regular.ttf").toExternalForm, 13)

    val file = new File(configLocation)
    if (file.exists && !file.isDirectory) {
      val lines = scala.io.Source.fromFile(file).getLines().mkString
      currentConfig = new Config(lines)
    } else {
      // Create new empty config file if not present
      val conf = new Config(List.empty[EnvironmentVariable].asJava)
      saveChangesToConfig.apply(conf)
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
      val allTabs = cardinalTabs.getTabs.asScala.filter(_.isInstanceOf[CardinalTabNew]).map(_.asInstanceOf[CardinalTabNew])
      val unsavedTabs = allTabs.count(_.hasUnsavedChanges)
      if (unsavedTabs > 0) {
        val noCallback = new function.Function[Void, Void] {
          override def apply(t: Void): Void = {
            null
          }
        }
        val cancelCallback = new function.Function[Void, Void] {
          override def apply(t: Void): Void = {
            r.consume()
            null
          }
        }
        showConfirmDialog.apply("Save all unsaved changes?", saveAllUnsavedChanges, noCallback, cancelCallback)
      }
    })

    primaryStage.setScene(scene)
    primaryStage.show()
  }

  private def setEnvironmentVariables(vars: List[EnvironmentVariable]): Unit = {
    currentConfig = currentConfig.withEnvironmentVariables(vars.asJava)
    saveChangesToConfig.apply(currentConfig)
  }

  private def triggerUnsavedChangesMade: java.util.function.Function[Void, Void] = {
    new java.util.function.Function[Void, Void] {
      override def apply(t: Void): Void = {
        getCurrentTab.handleUnsavedChangesMade()
        null
      }
    }
  }

  private def sendRequest: java.util.function.Function[CardinalRequest, CardinalResponse] = {
    new function.Function[CardinalRequest, CardinalResponse] {
      override def apply(request: CardinalRequest): CardinalResponse = {
        val startTime = System.currentTimeMillis()
        val response = new Http(request).send
        val totalTime = System.currentTimeMillis() - startTime
        new CardinalResponse(response, totalTime)
      }
    }
  }

  private def save: java.util.function.Function[Void, Void] = {
    new function.Function[Void, Void] {
      override def apply(t: Void): Void = {
        val currentTab = getCurrentTab
        if (currentTab != null && currentTab.getCurrentFile != null) {
          writeToFile(currentTab.getCurrentFile, currentTab.getContent.asInstanceOf[CardinalView].getRequest.toJson)
          currentTab.setUnsavedChanges(false)
          null
        } else {
          saveAs.apply(null)
        }
      }
    }
  }

  private def saveChangesToConfig: java.util.function.Function[Config, Void] = {
    new function.Function[Config, Void] {
      override def apply(config: Config): Void = {
        val configFile = new File(configLocation)
        writeToFile(configFile, config.toJson)
        null
      }
    }
  }

  private def clearAll: java.util.function.Function[Void, Void] = {
    new function.Function[Void, Void] {
      override def apply(t: Void): Void = {
        val currentTab = getCurrentTab
        if (currentTab != null) {
          currentTab.getContent.asInstanceOf[CardinalView].clearAll()
        }
        null
      }
    }
  }

  private def open: java.util.function.Function[Void, Void] = {
    new function.Function[Void, Void] {
      override def apply(t: Void): Void = {
        val fileChooser = new FileChooser
        val selectedFiles = fileChooser.showOpenMultipleDialog(currentStage)
        if (selectedFiles != null) {
          selectedFiles.asScala.foreach { selectedFile =>
            val lines = scala.io.Source.fromFile(selectedFile).getLines().mkString
            val cardinalView = new CardinalView(showAsCurl, showErrorDialog, getCurrentConfig, exportToCsv, exportToBash, sendRequest, triggerUnsavedChangesMade)
            addTab(new CardinalTabNew(selectedFile, cardinalView, openNewFileIfNoneOpen(), showConfirmDialog, save))
            cardinalView.loadRequest(CardinalRequest.apply(lines))
            getCurrentTab.setUnsavedChanges(false)
          }
        }
        null
      }
    }
  }

  private def newTab: java.util.function.Function[Void, Void] = {
    new java.util.function.Function[Void, Void] {
      override def apply(t: Void): Void = {
        cardinalTabs.getTabs.add(
          cardinalTabs.getTabs.size - 1,
          new CardinalTabNew(
            null,
            new CardinalView(showAsCurl, showErrorDialog, getCurrentConfig, exportToCsv, exportToBash, sendRequest, triggerUnsavedChangesMade), openNewFileIfNoneOpen(),
            showConfirmDialog,
            save
          )
        )
        cardinalTabs.getSelectionModel.select(cardinalTabs.getTabs.size() - 2)
        null
      }
    }
  }

  private def saveAs: java.util.function.Function[Void, Void] = {
    new java.util.function.Function[Void, Void] {
      override def apply(t: Void): Void = {
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

            if (currentTab.getCurrentFile == null) {
              // New file so just save file
              writeToFile(fileWithExtension, currentTab.getContent.asInstanceOf[CardinalView].getRequest.toJson)
              currentTab.setCurrentFile(fileWithExtension)
              currentTab.setUnsavedChanges(false)
            } else {
              // Existing file so save and open in new tab
              val request = currentTab.getContent.asInstanceOf[CardinalView].getRequest
              writeToFile(fileWithExtension, request.toJson)
              val cardinalView = new CardinalView(showAsCurl, showErrorDialog, getCurrentConfig, exportToCsv, exportToBash, sendRequest, triggerUnsavedChangesMade)
              addTab(new CardinalTabNew(fileWithExtension, cardinalView, openNewFileIfNoneOpen(), showConfirmDialog, save))
              cardinalView.loadRequest(request)
              getCurrentTab.setUnsavedChanges(false)
            }
          }
        }
        null
      }
    }
  }

  private def showConfirmDialog: QuadFunction[String, java.util.function.Function[Void, Void], java.util.function.Function[Void, Void], java.util.function.Function[Void, Void], Void] = {
    new QuadFunction[String, java.util.function.Function[Void, Void], java.util.function.Function[Void, Void], java.util.function.Function[Void, Void], Void] {
      override def apply(message: String, onYesCallback: java.util.function.Function[Void, Void], onNoCallback: java.util.function.Function[Void, Void], onCancelCallback: java.util.function.Function[Void, Void]): Void = {
        val alert = new Alert(AlertType.CONFIRMATION)
        alert.setTitle("Save Changes")
        alert.setContentText(message + "\n\n")

        val yesButton = new ButtonType("Yes", ButtonBar.ButtonData.RIGHT)
        val noButton = new ButtonType("No", ButtonBar.ButtonData.RIGHT)
        val cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.LEFT)
        alert.getButtonTypes.setAll(cancelButton, noButton, yesButton)

        val result = alert.showAndWait()
        if (result.get.getText == "Yes") {
          onYesCallback.apply(null)
        } else if (result.get.getText == "No") {
          onNoCallback.apply(null)
        } else if (result.get.getText == "Cancel") {
          onCancelCallback.apply(null)
        }
        null
      }
    }
  }

  def showBasicAuthInput: java.util.function.Function[Void, Void] = {
    new java.util.function.Function[Void, Void] {
      override def apply(t: Void): Void = {
        val currentTab = getCurrentTab
        if (currentTab != null) {
          val dialog = new BasicAuthInputDialog
          val result = dialog.showAndWait()
          if (result.isPresent) {
            currentTab.getContent.asInstanceOf[CardinalView].addHeader(result.get.asAuthHeader)
          }
        }
        null
      }
    }
  }

  def showFormUrlEncodedInput: java.util.function.Function[Void, Void] = {
    new java.util.function.Function[Void, Void] {
      override def apply(t: Void): Void = {
        val currentTab = getCurrentTab
        if (currentTab != null) {
          val dialog = new FormUrlEncodedInputDialog(currentTab.getContent.asInstanceOf[CardinalView].getRequest.getBody)
          val result = dialog.showAndWait()
          if (result.isPresent) {
            currentTab.getContent.asInstanceOf[CardinalView].setBody(result.get.toString)
            currentTab.getContent.asInstanceOf[CardinalView].addHeader(result.get.header)
          }
        }
        null
      }
    }
  }

  def showEnvironmentVariablesInput: java.util.function.Function[Void, Void] = {
    new java.util.function.Function[Void, Void] {
      override def apply(t: Void): Void = {
        val dialog = new EnvironmentVariablesEditDialog(currentConfig.getEnvironmentVariables)
        val results = dialog.showAndWait()
        if (results.isPresent) {
          setEnvironmentVariables(results.get.asScala.toList)
        }
        null
      }
    }
  }

  def showErrorDialog: java.util.function.Function[String, Void] = {
    new function.Function[String, Void] {
      override def apply(errorMessage: String): Void = {
        Platform.runLater(() => {
          val alert = new Alert(AlertType.ERROR)
          alert.setContentText(errorMessage)
          alert.showAndWait
        })
        null
      }
    }
  }

  def getCurrentConfig: java.util.function.Function[Void, Config] = {
    new java.util.function.Function[Void, Config] {
      override def apply(t: Void): Config = {
        currentConfig
      }
    }
  }

  def showAsCurl: java.util.function.Function[Void, Void] = {
    new function.Function[Void, Void] {
      override def apply(t: Void): Void = {
        val currentTab = getCurrentTab
        if (currentTab != null) {
          val request = currentTab.getContent.asInstanceOf[CardinalView].getRequest
          if (request.getUri.trim.length == 0) {
            showErrorDialog.apply("Please enter a URL.")
          } else {
            currentTab.getContent.asInstanceOf[CardinalView].loadCurlCommand(request.toCurl(currentConfig))
          }
        }
        null
      }
    }
  }

  private def getCurrentTab: CardinalTabNew = {
    cardinalTabs.getSelectionModel.getSelectedItem.asInstanceOf[CardinalTabNew]
  }

  def saveAllUnsavedChanges(): java.util.function.Function[Void, Void] = {
    new java.util.function.Function[Void, Void] {
      override def apply(t: Void): Void = {
        cardinalTabs.getTabs.asScala.foreach {
          case t: CardinalTabNew =>
            val currentFile = t.getCurrentFile
            if (currentFile != null) {
              writeToFile(currentFile, t.getContent.asInstanceOf[CardinalView].getRequest.toJson)
            }
          case _ => // Currently does not save unsaved changes to "Untitled" files
        }
        null
      }
    }
  }

  def writeToFile(file: File, data: String): Unit = {
    val fileWriter = new FileWriter(file)
    fileWriter.write(data)
    fileWriter.close()
  }

  def openNewFileIfNoneOpen(): java.util.function.Function[Void, Void] = {
    new java.util.function.Function[Void, Void] {
      override def apply(t: Void): Void = {
        if (cardinalTabs.getTabs.size() == 0) {
          newTab.apply(null)
        }
        null
      }
    }
  }

  def exportToCsv(): java.util.function.Function[java.util.List[CardinalRequestAndResponse], Void] = {
    new java.util.function.Function[java.util.List[CardinalRequestAndResponse], Void] {
      override def apply(requestAndResponses: util.List[CardinalRequestAndResponse]): Void = {
        val header = CardinalRequest.csvHeaders + "," + CardinalResponse.csvHeaders()
        val content = requestAndResponses.asScala.map { reqAndRes =>
          reqAndRes.getRequest.toCsv + "," + (if (reqAndRes.getResponse != null) reqAndRes.getResponse.toCSV else CardinalResponse.blank().toCSV)
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
        null
      }
    }
  }

  def exportToBash: java.util.function.BiFunction[java.util.List[CardinalRequest], java.lang.Integer, Void] = {
    new java.util.function.BiFunction[java.util.List[CardinalRequest], java.lang.Integer, Void] {
      override def apply(requests: java.util.List[CardinalRequest], throttle: java.lang.Integer): Void = {
        val bashScript = new BashScript(requests, currentConfig, throttle)
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
        null
      }
    }
  }

  def addTab(tab: Tab): Unit = {
    cardinalTabs.getTabs.add(cardinalTabs.getTabs.size - 1, tab)
    cardinalTabs.getSelectionModel.select(cardinalTabs.getTabs.size() - 2)
  }

}
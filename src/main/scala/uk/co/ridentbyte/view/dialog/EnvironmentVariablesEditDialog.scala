package uk.co.ridentbyte.view.dialog

import javafx.geometry.HPos
import javafx.scene.control.ScrollPane.ScrollBarPolicy
import javafx.scene.control._
import javafx.scene.layout.{GridPane, Priority}
import uk.co.ridentbyte.model.EnvironmentVariable
import uk.co.ridentbyte.view.util.ColumnConstraintsBuilder

class EnvironmentVariablesEditDialog(existingParameters: List[EnvironmentVariable] = List.empty[EnvironmentVariable])
  extends Dialog[List[EnvironmentVariable]] {

  setTitle("Edit Environment Variables")
  getDialogPane.getButtonTypes.addAll(ButtonType.CANCEL, ButtonType.OK)


  private val grid = new GridPane
  grid.setHgap(10)
  grid.setVgap(10)
  grid.setPrefWidth(450)
  grid.getColumnConstraints.addAll(
    ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build,
    ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build,
    ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build,
    ColumnConstraintsBuilder().withHgrow(Priority.NEVER).build
  )

  private val scrollPane = new ScrollPane()
  scrollPane.setPrefViewportHeight(300)
  scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER)
  scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS)
  scrollPane.setPrefViewportWidth(grid.getPrefWidth)
  scrollPane.setContent(grid)
  getDialogPane.setContent(scrollPane)

  private val keyValueInputs = scala.collection.mutable.ListBuffer.empty[KeyValueInput]

  if (existingParameters.isEmpty) {
    keyValueInputs += KeyValueInput("", "")
    keyValueInputs += KeyValueInput("", "")
    keyValueInputs += KeyValueInput("", "")
    keyValueInputs += KeyValueInput("", "")
    keyValueInputs += KeyValueInput("", "")
  } else {
    existingParameters.foreach { existingParameter =>
      keyValueInputs += KeyValueInput(existingParameter.key, existingParameter.value)
    }
    if (existingParameters.length < 5) {
      existingParameters.length.to(5).foreach { _ =>
        keyValueInputs += KeyValueInput("", "")
      }
    }
  }

  keyValueInputs.indices.foreach { i =>
    keyValueInputs(i).addTo(grid, i, 0)
  }

  val buttonRemove = new Button("-")
  buttonRemove.setOnAction(_ => {
    if (keyValueInputs.nonEmpty) {
      keyValueInputs.remove(keyValueInputs.length - 1)
      render()
      scrollPane.setVvalue(1)
    }
  })
  GridPane.setHalignment(buttonRemove, HPos.RIGHT)

  val buttonAdd = new Button("+")
  buttonAdd.setOnAction(_ => {
    keyValueInputs += KeyValueInput("", "")
    render()
    scrollPane.setVvalue(1)
  })
  GridPane.setHalignment(buttonAdd, HPos.RIGHT)

  render()

  setResultConverter(buttonType => {
    if (buttonType == ButtonType.OK) {
      val keyValueInputsWithValues = keyValueInputs.filter { kv =>
        kv.textKey.getText.trim.length != 0 || kv.textValue.getText.trim.length != 0
      }

      keyValueInputsWithValues.map { keyValueInput =>
        EnvironmentVariable(keyValueInput.textKey.getText, keyValueInput.textValue.getText)
      }.toList
    } else {
      null
    }
  })

  private def render(): Unit = {
    grid.getChildren.clear()
    keyValueInputs.indices.foreach { i =>
      keyValueInputs(i).addTo(grid, 0, i)
    }
    grid.add(buttonRemove, 2, keyValueInputs.length)
    grid.add(buttonAdd, 3, keyValueInputs.length)
    getDialogPane.getScene.getWindow.sizeToScene()
    grid.setGridLinesVisible(true)
  }
}

case class KeyValueInput(private val key: String, private val value: String) {
  val textKey = new TextField(key)
  textKey.getStyleClass.add("cardinal-font")

  val textValue = new TextField(value)
  textValue.getStyleClass.add("cardinal-font")

  GridPane.setColumnSpan(textValue, 2)

  def addTo(grid: GridPane, x: Int, y: Int): Unit = {
    grid.add(textKey, x, y)
    val labelEquals = new Label("=")
    GridPane.setHalignment(labelEquals, HPos.CENTER)
    grid.add(labelEquals, x + 1, y)
    grid.add(textValue, x + 2, y)
  }
}

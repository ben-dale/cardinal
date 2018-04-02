package uk.co.ridentbyte.view.dialog

import javafx.scene.control._
import javafx.scene.layout.GridPane

class EnvironmentVariablesEditDialog(existingParameters: List[String] = List.empty[String]) extends Dialog[List[String]] {

  setTitle("Edit Environment Variables")

  getDialogPane.getButtonTypes.addAll(ButtonType.CANCEL, ButtonType.OK)

  private val grid = new GridPane
  grid.setHgap(10)
  grid.setVgap(10)

  private val labelPrompt = new Label("One variable per line, in the format: 'key=value'")
  grid.add(labelPrompt, 0, 0)

  private val textVariables = new TextArea()
  textVariables.setText(existingParameters.mkString("\n"))
  textVariables.setPrefHeight(300)
  textVariables.setPrefWidth(500)
  textVariables.setStyle(
    """
      |-fx-font-family: Monospaced;
      |-fx-font-size: 13;
      |-fx-font-weight: 600;
    """.stripMargin
  )
  grid.add(textVariables, 0, 1)

  getDialogPane.setContent(grid)

  setResultConverter((buttonType) => {
    if (buttonType == ButtonType.OK) {
      textVariables.getText.split("\n").toList
    } else {
      null
    }
  })
}

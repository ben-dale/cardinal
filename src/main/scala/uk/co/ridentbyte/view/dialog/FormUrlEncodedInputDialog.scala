package uk.co.ridentbyte.view.dialog

import javafx.scene.control._
import javafx.scene.layout.GridPane
import uk.co.ridentbyte.model.FormUrlEncodedParameters

class FormUrlEncodedInputDialog(existingParameters: String = "") extends Dialog[FormUrlEncodedParameters] {

  setTitle("Basic Auth")

  getDialogPane.getButtonTypes.addAll(ButtonType.CANCEL, ButtonType.OK)

  private val grid = new GridPane
  grid.setHgap(10)
  grid.setVgap(10)

  private val labelUsername = new Label("One parameter per line, in the format: 'key=value'")
  grid.add(labelUsername, 0, 0)

  private val textParameters = new TextArea()
  textParameters.setText(FormUrlEncodedParameters(existingParameters).toBodyEditString)
  textParameters.setPrefHeight(300)
  textParameters.setPrefWidth(500)
  textParameters.setStyle(
    """
      |-fx-font-family: Monospaced;
      |-fx-font-size: 13;
      |-fx-font-weight: 600;
    """.stripMargin
  )
  grid.add(textParameters, 0, 1)

  getDialogPane.setContent(grid)

  setResultConverter((buttonType) => {
    if (buttonType == ButtonType.OK) {
      FormUrlEncodedParameters(textParameters.getText.split("\n").toList)
    } else {
      null
    }
  })
}

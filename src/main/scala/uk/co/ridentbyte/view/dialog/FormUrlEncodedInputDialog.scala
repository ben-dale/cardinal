package uk.co.ridentbyte.view.dialog

import javafx.scene.control._
import javafx.scene.layout.GridPane
import uk.co.ridentbyte.model.FormUrlEncoded

class FormUrlEncodedInputDialog(existingParameters: String = "") extends Dialog[FormUrlEncoded] {

  setTitle("Basic Auth")

  getDialogPane.getButtonTypes.addAll(ButtonType.CANCEL, ButtonType.OK)

  private val grid = new GridPane
  grid.setHgap(10)
  grid.setVgap(10)

  private val labelUsername = new Label("One parameter per line, in the format: 'key=value'")
  grid.add(labelUsername, 0, 0)

  private val textParameters = new TextArea()
  textParameters.setText(FormUrlEncoded(existingParameters).lines)
  textParameters.setPrefHeight(300)
  textParameters.setPrefWidth(500)
  textParameters.getStyleClass.add("cardinal-font")
  grid.add(textParameters, 0, 1)

  getDialogPane.setContent(grid)

  setResultConverter(buttonType => {
    if (buttonType == ButtonType.OK) {
      FormUrlEncoded(textParameters.getText.split("\n").toList)
    } else {
      null
    }
  })
}

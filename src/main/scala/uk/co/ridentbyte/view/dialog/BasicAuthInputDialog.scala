package uk.co.ridentbyte.view.dialog

import javafx.application.Platform
import javafx.geometry.HPos
import javafx.scene.control.{ButtonType, Dialog, Label, TextField}
import javafx.scene.layout.GridPane
import uk.co.ridentbyte.model.BasicAuth

class BasicAuthInputDialog extends Dialog[BasicAuth] {

  setTitle("Basic Auth")

  getDialogPane.getButtonTypes.addAll(ButtonType.CANCEL, ButtonType.OK)

  private val grid = new GridPane
  grid.setHgap(10)
  grid.setVgap(10)

  private val labelUsername = new Label("Username")
  GridPane.setHalignment(labelUsername, HPos.RIGHT)
  grid.add(labelUsername, 0, 0)

  private val textUsername = new TextField
  grid.add(textUsername, 1, 0)

  private val labelPassword = new Label("Password")
  GridPane.setHalignment(labelPassword, HPos.RIGHT)
  grid.add(labelPassword, 0, 1)

  private val textPassword = new TextField
  grid.add(textPassword, 1, 1)

  getDialogPane.setContent(grid)

  Platform.runLater(() => textUsername.requestFocus())

  setResultConverter(buttonType => {
    if (buttonType == ButtonType.OK) {
      BasicAuth(textUsername.getText, textPassword.getText)
    } else {
      null
    }
  })
}

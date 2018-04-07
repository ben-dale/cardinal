package uk.co.ridentbyte.view.request

import javafx.collections.FXCollections
import javafx.scene.control.{ChoiceBox, TextField}
import javafx.scene.layout.{GridPane, Priority}

import scala.collection.JavaConverters._

class RequestUriVerbInputPane(triggerUnsavedChangesMade: () => Unit) extends GridPane {

  setHgap(10)
  setVgap(10)

  private val textUri = new TextField()
  textUri.getStyleClass.add("cardinal-font")
  textUri.textProperty().addListener((_, _, _) => triggerUnsavedChangesMade())
  GridPane.setVgrow(textUri, Priority.NEVER)
  GridPane.setHgrow(textUri, Priority.ALWAYS)
  textUri.setPromptText("http://localhost:8080")
  add(textUri, 0, 0)

  private val selectVerb = new ChoiceBox[String](FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE", "HEAD", "CONNECT", "OPTIONS", "TRACE", "PATCH"))
  selectVerb.getStyleClass.add("cardinal-font")
  selectVerb.getSelectionModel.selectFirst()
  selectVerb.getSelectionModel.selectedItemProperty.addListener((_, _, _) => triggerUnsavedChangesMade())
  GridPane.setVgrow(selectVerb, Priority.NEVER)
  GridPane.setHgrow(selectVerb, Priority.NEVER)
  add(selectVerb, 1, 0)

  def getUri: String =  textUri.getText.trim

  def getVerb: String = selectVerb.getSelectionModel.getSelectedItem

  def setUri(uri: String): Unit = textUri.setText(uri)

  def setVerb(verb: String): Unit = {
    val matchingIndex = selectVerb.getItems.asScala.zipWithIndex.find {
      case (item: String, _) => item == verb
    }.map { verbWithIndex =>
      verbWithIndex._2
    }.getOrElse(0)
    selectVerb.getSelectionModel.select(matchingIndex)
  }

  def clear(): Unit = {
    textUri.setText("")
    selectVerb.getSelectionModel.select(0)
  }

}

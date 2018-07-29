package uk.co.ridentbyte.view.response

import javafx.geometry.Insets
import javafx.scene.control.TextArea
import javafx.scene.layout.{GridPane, Priority}

case class CurlOutputPane(command: String) extends GridPane {

  setHgap(10)
  setVgap(10)
  setPadding(new Insets(20))
  getStyleClass.addAll("plain-border", "round-border")

  val textAreaCurl = new TextArea()
  textAreaCurl.getStyleClass.addAll("cardinal-font-console", "curl-output")
  textAreaCurl.setText(command)
  textAreaCurl.setWrapText(true)
  GridPane.setHgrow(textAreaCurl, Priority.ALWAYS)
  GridPane.setVgrow(textAreaCurl, Priority.ALWAYS)
  add(textAreaCurl, 0, 0)
}

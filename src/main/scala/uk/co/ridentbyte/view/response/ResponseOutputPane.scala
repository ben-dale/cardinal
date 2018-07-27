package uk.co.ridentbyte.view.response

import javafx.scene.control.{ListView, TextArea}
import javafx.scene.layout.{GridPane, Priority}
import uk.co.ridentbyte.model.CardinalResponse
import uk.co.ridentbyte.view.util.{ColumnConstraintsBuilder, RowConstraintsBuilder}

case class ResponseOutputPane(response: CardinalResponse) extends GridPane {

  setVgap(10)
  setHgap(10)
  getColumnConstraints.add(ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build)
  getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).withPercentageHeight(40).build)
  getRowConstraints.add(RowConstraintsBuilder().withVgrow(Priority.ALWAYS).withPercentageHeight(60).build)

  val listHeaders = new ListView[String]()
  listHeaders.getStyleClass.add("cardinal-font")
  response.raw.headers.foreach {
    header => listHeaders.getItems.add(header._1 + ": " + header._2.mkString(""))
  }
  add(listHeaders, 0, 0)

  val textAreaBody = new TextArea()
  textAreaBody.setEditable(false)
  textAreaBody.getStyleClass.add("cardinal-font")
  textAreaBody.setText(response.formattedBody)
  add(textAreaBody, 0, 1)

}

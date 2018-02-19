package uk.co.ridentbyte

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Stage

object Cardinal {
  Application.launch()
}

class Cardinal extends Application {
  override def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("Cardinal")
    val root = new StackPane()
    primaryStage.setScene(new Scene(root, 300, 250))
    primaryStage.show()
  }
}
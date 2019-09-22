package uk.co.ridentbyte.view.response;

import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import uk.co.ridentbyte.model.CardinalResponse;

class ResponseOutputPane extends GridPane {

    ResponseOutputPane(CardinalResponse response) {
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(20));
        this.getStyleClass().addAll("plain-border", "round-border");

        var textArea = new TextArea();
        textArea.setEditable(false);
        textArea.getStyleClass().addAll("cardinal-font-console", "console-output");
        textArea.setText(response.rawHttpResponseText());
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        this.add(textArea, 0, 0);
    }

}

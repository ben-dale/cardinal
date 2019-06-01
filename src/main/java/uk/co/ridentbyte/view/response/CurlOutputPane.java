package uk.co.ridentbyte.view.response;

import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class CurlOutputPane extends GridPane {

    public CurlOutputPane(String command) {
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(20));
        this.getStyleClass().addAll("plain-border", "round-border");

        TextArea textAreaCurl = new TextArea();
        textAreaCurl.getStyleClass().addAll("cardinal-font-console", "curl-output");
        textAreaCurl.setText(command);
        textAreaCurl.setWrapText(true);
        GridPane.setHgrow(textAreaCurl, Priority.ALWAYS);
        GridPane.setVgrow(textAreaCurl, Priority.ALWAYS);
        add(textAreaCurl, 0, 0);
    }

}

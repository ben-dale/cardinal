package uk.co.ridentbyte.view.response;

import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class ClearResponsePane extends BorderPane {
    public ClearResponsePane() {
        var prompt = new Label("Welcome!");
        prompt.getStyleClass().addAll("cardinal-font-console", "grey-text");
        this.getStyleClass().addAll("dashed-border", "round-border");
        this.setCenter(prompt);
        this.setBorder(
                new Border(
                        new BorderStroke(
                                Color.LIGHTGREY,
                                BorderStrokeStyle.DASHED,
                                CornerRadii.EMPTY,
                                BorderWidths.DEFAULT
                        )
                )
        );
    }
}

package uk.co.ridentbyte.view.response;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class ClearResponsePane extends BorderPane {
    public ClearResponsePane() {
        this.getStyleClass().addAll("dashed-border", "round-border");
        this.setBorder(new Border(new BorderStroke(Color.LIGHTGREY, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }
}

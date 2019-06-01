package uk.co.ridentbyte.view.dialog;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import uk.co.ridentbyte.model.FormUrlEncoded;

public class FormUrlEncodedInputDialog extends Dialog<FormUrlEncoded> {

    public FormUrlEncodedInputDialog(String existingParameters) {
        this.setTitle("Form Parameters");
        this.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Label labelUsername = new Label("One parameter per line, in the format: 'key=value'");
        grid.add(labelUsername, 0, 0);

        TextArea textParameters = new TextArea();
        textParameters.setText(new FormUrlEncoded(existingParameters).lines());
        textParameters.setPrefHeight(300);
        textParameters.setPrefWidth(500);
        textParameters.getStyleClass().add("cardinal-font");
        grid.add(textParameters, 0, 1);

        this.getDialogPane().setContent(grid);

        setResultConverter((buttonType) -> {
            if (buttonType == ButtonType.OK) {
                return new FormUrlEncoded(textParameters.getText().split("\n"));
            } else {
                return null;
            }
        });
    }
}

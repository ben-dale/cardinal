package uk.co.ridentbyte.view.dialog;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import uk.co.ridentbyte.model.BasicAuth;

public class BasicAuthInputDialog extends Dialog<BasicAuth> {

    public BasicAuthInputDialog() {
        this.setTitle("Basic Auth");
        this.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Label labelUsername = new Label("Username");
        GridPane.setHalignment(labelUsername, HPos.RIGHT);
        grid.add(labelUsername, 0, 0);

        TextField textUsername = new TextField();
        grid.add(textUsername, 1, 0);

        Label labelPassword = new Label("Password");
        GridPane.setHalignment(labelUsername, HPos.RIGHT);
        grid.add(labelPassword, 0, 1);

        TextField textPassword = new TextField();
        grid.add(textPassword, 1, 1);

        this.getDialogPane().setContent(grid);

        Platform.runLater(textUsername::requestFocus);

        this.setResultConverter((buttonType) -> {
            if (buttonType == ButtonType.OK) {
                return new BasicAuth(textUsername.getText(), textPassword.getText());
            } else {
                return null;
            }
        });
    }

}

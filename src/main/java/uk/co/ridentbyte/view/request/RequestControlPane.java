package uk.co.ridentbyte.view.request;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import uk.co.ridentbyte.functions.ShowAsCurl;
import uk.co.ridentbyte.functions.ShowBulkRequestInput;

import java.util.function.BiFunction;
import java.util.function.Function;

public class RequestControlPane extends GridPane {

    private final Button buttonSendRequest;

    public RequestControlPane(ShowAsCurl showAsCurl,
                              ShowBulkRequestInput showBulkRequest,
                              BiFunction<Function<Void, Void>, Function<Void, Void>, Void> sendRequest) {
        this.setHgap(10);
        this.setVgap(10);
        this.getStyleClass().add("control-pane");

        Button buttonExportToCurl = new Button("As cURL");
        buttonExportToCurl.getStyleClass().add("cardinal-font");
        GridPane.setVgrow(buttonExportToCurl, Priority.NEVER);
        GridPane.setHgrow(buttonExportToCurl, Priority.ALWAYS);
        GridPane.setHalignment(buttonExportToCurl, HPos.RIGHT);
        buttonExportToCurl.setOnAction((actionEvent) -> showAsCurl.trigger());
        this.add(buttonExportToCurl, 0, 0);

        Button buttonSendBulkRequest = new Button("Send Bulk Request...");
        buttonSendBulkRequest.getStyleClass().add("cardinal-font");
        GridPane.setVgrow(buttonSendBulkRequest, Priority.NEVER);
        GridPane.setHgrow(buttonSendBulkRequest, Priority.NEVER);
        buttonSendBulkRequest.setOnAction((actionEvent) -> showBulkRequest.show());
        this.add(buttonSendBulkRequest, 1, 0);

        buttonSendRequest = new Button("Send Request");
        buttonSendRequest.setMinWidth(120);
        buttonSendRequest.setMaxWidth(120);
        buttonSendRequest.setPrefWidth(120);
        buttonSendRequest.getStyleClass().add("cardinal-font");
        GridPane.setVgrow(buttonSendRequest, Priority.NEVER);
        GridPane.setHgrow(buttonSendRequest, Priority.NEVER);
        buttonSendRequest.setOnAction((actionEvent) -> {
            sendRequest.apply((a) -> {
                Platform.runLater(() -> {
                    buttonSendRequest.setText("Sending...");
                    buttonSendRequest.setDisable(true);
                });
                return null;
            }, (a) -> {
                Platform.runLater(() -> {
                    buttonSendRequest.setText("Send Request");
                    buttonSendRequest.setDisable(false);
                });
                return null;
            });
        });
        this.add(buttonSendRequest, 2, 0);
    }

    public void triggerSendRequest() {
        buttonSendRequest.fire();
    }
}

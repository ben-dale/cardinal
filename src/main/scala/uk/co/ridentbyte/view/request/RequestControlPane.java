package uk.co.ridentbyte.view.request;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import scala.Function0;
import scala.Function2;
import scala.runtime.BoxedUnit;

public class RequestControlPane extends GridPane {

    public RequestControlPane(Function0<BoxedUnit> exportToCurl,
                              Function0<BoxedUnit> showBulkRequest,
                              Function2<Function0<BoxedUnit>, Function0<BoxedUnit>, BoxedUnit> sendRequest) {
        this.setHgap(10);
        this.setVgap(10);
        this.getStyleClass().add("control-pane");

        Button buttonExportToCurl = new Button("As cURL");
        buttonExportToCurl.getStyleClass().add("cardinal-font");
        GridPane.setVgrow(buttonExportToCurl, Priority.NEVER);
        GridPane.setHgrow(buttonExportToCurl, Priority.ALWAYS);
        GridPane.setHalignment(buttonExportToCurl, HPos.RIGHT);
        buttonExportToCurl.setOnAction((actionEvent) -> exportToCurl.apply());
        this.add(buttonExportToCurl, 0, 0);

        Button buttonSendBulkRequest = new Button("Send Bulk Request...");
        buttonSendBulkRequest.getStyleClass().add("cardinal-font");
        GridPane.setVgrow(buttonSendBulkRequest, Priority.NEVER);
        GridPane.setHgrow(buttonSendBulkRequest, Priority.NEVER);
        buttonSendBulkRequest.setOnAction((actionEvent) -> showBulkRequest.apply());
        this.add(buttonSendBulkRequest, 1, 0);

        Button buttonSendRequest = new Button("Send Request");
        buttonSendRequest.setMinWidth(120);
        buttonSendRequest.setMaxWidth(120);
        buttonSendRequest.setPrefWidth(120);
        buttonSendRequest.getStyleClass().add("cardinal-font");
        GridPane.setVgrow(buttonSendRequest, Priority.NEVER);
        GridPane.setHgrow(buttonSendRequest, Priority.NEVER);
        buttonSendRequest.setOnAction((actionEvent) -> {
            sendRequest.apply(() -> {
                Platform.runLater(() -> {
                    buttonSendRequest.setText("Sending...");
                    buttonSendRequest.setDisable(true);
                });
                return BoxedUnit.UNIT;
            }, () -> {
                Platform.runLater(() -> {
                    buttonSendRequest.setText("Send Request");
                    buttonSendRequest.setDisable(false);
                });
                return BoxedUnit.UNIT;
            });
        });
        this.add(buttonSendRequest, 2, 0);

    }
}

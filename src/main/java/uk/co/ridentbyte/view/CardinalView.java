package uk.co.ridentbyte.view;

import javafx.application.Platform;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import uk.co.ridentbyte.functions.ExportToBash;
import uk.co.ridentbyte.functions.ExportToCSV;
import uk.co.ridentbyte.functions.ShowAsCurl;
import uk.co.ridentbyte.functions.ShowErrorDialog;
import uk.co.ridentbyte.functions.UnsavedChangesMade;
import uk.co.ridentbyte.model.CardinalRequest;
import uk.co.ridentbyte.model.CardinalResponse;
import uk.co.ridentbyte.model.Config;
import uk.co.ridentbyte.model.Vocabulary;
import uk.co.ridentbyte.view.request.RequestControlPane;
import uk.co.ridentbyte.view.request.RequestInputPane;
import uk.co.ridentbyte.view.response.ResponsePane;
import uk.co.ridentbyte.view.util.ColumnConstraintsBuilder;
import uk.co.ridentbyte.view.util.RowConstraintsBuilder;

import java.util.function.BiFunction;
import java.util.function.Function;

public class CardinalView extends BorderPane {

    private RequestInputPane requestPane;
    private ResponsePane responsePane;

    private ShowErrorDialog showErrorDialog;
    private Function<CardinalRequest, CardinalResponse> sendRequest;
    private Function<Void, Config> getConfig;
    private final RequestControlPane requestControlPane;

    public CardinalView(ShowAsCurl showAsCurl,
                        ShowErrorDialog showErrorDialog,
                        Function<Void, Config> getConfig,
                        ExportToCSV exportToCsv,
                        ExportToBash exportToBash,
                        Function<CardinalRequest, CardinalResponse> sendRequest,
                        UnsavedChangesMade triggerUnsavedChangesMade,
                        Vocabulary vocabulary) {

        this.showErrorDialog = showErrorDialog;
        this.sendRequest = sendRequest;
        this.getConfig = getConfig;

        var requestResponseSplitPane = new SplitPane();
        requestResponseSplitPane.setDividerPositions(0.4);

        this.requestPane = new RequestInputPane(triggerUnsavedChangesMade, vocabulary);
        requestPane.setMinWidth(400);

        this.responsePane = new ResponsePane(getConfig, sendRequest, exportToCsv, exportToBash, showErrorDialog);
        responsePane.setMinWidth(400);

        requestControlPane = new RequestControlPane(showAsCurl, this::showBulkRequestInput, this.sendSingleRequest());

        var gridPane = new GridPane();
        gridPane.getColumnConstraints().add(new ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build());
        gridPane.getRowConstraints().add(new RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build());
        gridPane.getRowConstraints().add(new RowConstraintsBuilder().withVgrow(Priority.NEVER).build());

        requestResponseSplitPane.getItems().add(requestPane);
        requestResponseSplitPane.getItems().add(responsePane);

        gridPane.add(requestResponseSplitPane, 0, 0);
        gridPane.add(requestControlPane, 0, 1);

        this.setCenter(gridPane);
    }

    public void clearAll() {
        this.requestPane.clear();
        this.responsePane.clear();
    }

    public void loadRequest(CardinalRequest request) {
        this.requestPane.loadRequest(request);
    }

    public void loadCurlCommand(String curl) {
        this.responsePane.loadCurlCommand(curl);
    }

    public CardinalRequest getRequest() {
        return this.requestPane.getRequest();
    }

    public void setBody(String body) {
        this.requestPane.setBody(body);
    }

    public void addHeader(String header) {
        this.requestPane.addHeader(header);
    }

    private void showBulkRequestInput() {
        var request = requestPane.getRequest();
        if (request.getUri().trim().length() == 0) {
            showErrorDialog.show("Please enter a URL.");
        } else {
            responsePane.showBulkRequestInput(requestPane.getRequest());
        }
    }

    private BiFunction<Function<Void, Void>, Function<Void, Void>, Void> sendSingleRequest() {
        return new BiFunction<Function<Void, Void>, Function<Void, Void>, Void>() {
            @Override
            public Void apply(Function<Void, Void> onStart, Function<Void, Void> onFinish) {
                Platform.runLater(() -> responsePane.clear());
                CardinalRequest request = requestPane.getRequest();
                if (request.getUri().trim().length() == 0) {
                    showErrorDialog.show("Please enter a URL.");
                } else {
                    new Thread(() -> {
                        Platform.runLater(() -> onStart.apply(null));
                        try {
                            CardinalResponse response = sendRequest.apply(requestPane.getRequest().processConstants(getConfig.apply(null)));
                            responsePane.setResponse(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showErrorDialog.show("Unknown error");
                        } finally {
                            Platform.runLater(() -> onFinish.apply(null));
                        }
                    }).start();
                }
                return null;
            }
        };
    }

    public void triggerSendRequest() {
        requestControlPane.triggerSendRequest();
    }

}

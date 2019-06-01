package uk.co.ridentbyte.view;

import javafx.application.Platform;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import uk.co.ridentbyte.model.CardinalRequestAndResponse;
import uk.co.ridentbyte.model.CardinalRequest;
import uk.co.ridentbyte.model.CardinalResponse;
import uk.co.ridentbyte.model.Config;
import uk.co.ridentbyte.view.request.RequestControlPane;
import uk.co.ridentbyte.view.request.RequestInputPane;
import uk.co.ridentbyte.view.response.ResponsePane;
import uk.co.ridentbyte.view.util.ColumnConstraintsBuilder;
import uk.co.ridentbyte.view.util.RowConstraintsBuilder;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CardinalView extends BorderPane {

    private RequestInputPane requestInputPane;
    private ResponsePane responsePane;

    private Function<String, Void> showErrorDialog;
    private Function<CardinalRequest, CardinalResponse> sendRequest;
    private Function<Void, Config> getConfig;

    public CardinalView(Function<Void, Void> exportToCurl,
                        Function<String, Void> showErrorDialog,
                        Function<Void, Config> getConfig,
                        Function<List<CardinalRequestAndResponse>, Void> exportToCsv,
                        BiFunction<List<CardinalRequest>, Integer, Void> exportToBash,
                        Function<CardinalRequest, CardinalResponse> sendRequest,
                        Function<Void, Void> triggerUnsavedChangesMade) {

        this.showErrorDialog = showErrorDialog;
        this.sendRequest = sendRequest;
        this.getConfig = getConfig;

        SplitPane requestResponseSplitPane = new SplitPane();
        requestResponseSplitPane.setDividerPositions(0.4);

        this.requestInputPane = new RequestInputPane(triggerUnsavedChangesMade);
        requestInputPane.setMinWidth(400);

        this.responsePane = new ResponsePane(getConfig, sendRequest, exportToCsv, exportToBash, showErrorDialog);
        responsePane.setMinWidth(400);

        RequestControlPane requestControlPane = new RequestControlPane(exportToCurl, this.showBulkRequestInput(), this.sendSingleRequest());

        GridPane grid = new GridPane();
        grid.getColumnConstraints().add(new ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build());
        grid.getRowConstraints().add(new RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build());
        grid.getRowConstraints().add(new RowConstraintsBuilder().withVgrow(Priority.NEVER).build());

        requestResponseSplitPane.getItems().add(requestInputPane);
        requestResponseSplitPane.getItems().add(responsePane);

        grid.add(requestResponseSplitPane, 0, 0);
        grid.add(requestControlPane, 0, 1);

        this.setCenter(grid);
    }

    public void clearAll() {
        this.requestInputPane.clear();
        this.responsePane.clearContents();
    }

    public void loadRequest(CardinalRequest request) {
        this.requestInputPane.loadRequest(request);
    }

    public void loadCurlCommand(String curl) {
        this.responsePane.loadCurlCommand(curl);
    }

    public CardinalRequest getRequest() {
        return this.requestInputPane.getRequest();
    }

    public void setBody(String body) {
        this.requestInputPane.setBody(body);
    }

    public void addHeader(String header) {
        this.requestInputPane.addHeader(header);
    }

    private Function<Void, Void> showBulkRequestInput() {
        return new Function<Void, Void>() {
            @Override
            public Void apply(Void aVoid) {
                CardinalRequest request = requestInputPane.getRequest();
                if (request.getUri().trim().length() == 0) {
                    showErrorDialog.apply("Please enter a URL.");
                } else {
                    responsePane.showBulkRequestInput(requestInputPane.getRequest());
                }
                return null;
            }
        };
    }

    private BiFunction<Function<Void, Void>, Function<Void, Void>, Void> sendSingleRequest() {
        return new BiFunction<Function<Void, Void>, Function<Void, Void>, Void>() {
            @Override
            public Void apply(Function<Void, Void> onStart, Function<Void, Void> onFinish) {
                Platform.runLater(() -> responsePane.clearContents());
                CardinalRequest request = requestInputPane.getRequest();
                if (request.getUri().trim().length() == 0) {
                    showErrorDialog.apply("Please enter a URL.");
                } else {
                    new Thread(() -> {
                        Platform.runLater(() -> onStart.apply(null));
                        try {
                            CardinalResponse response = sendRequest.apply(requestInputPane.getRequest().processConstants(getConfig.apply(null)));
                            responsePane.setResponse(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showErrorDialog.apply("Unknown error");
                        } finally {
                            Platform.runLater(() -> onFinish.apply(null));
                        }
                    }).start();
                }
                return null;
            }
        };
    }

}
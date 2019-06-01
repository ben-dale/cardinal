package uk.co.ridentbyte.view.response;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import uk.co.ridentbyte.model.CardinalBulkRequest;
import uk.co.ridentbyte.model.CardinalRequestAndResponse;
import uk.co.ridentbyte.model.CardinalRequest;
import uk.co.ridentbyte.model.CardinalResponse;
import uk.co.ridentbyte.model.Config;
import uk.co.ridentbyte.view.util.ColumnConstraintsBuilder;
import uk.co.ridentbyte.view.util.RowConstraintsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BulkRequestProcessingOutputPane extends GridPane {

    private Task<Boolean> task;

    public BulkRequestProcessingOutputPane(Function<Void, Config> getConfig,
                                           Function<CardinalRequest, CardinalResponse> sendRequestCallback,
                                           BiFunction<List<CardinalRequestAndResponse>, Integer, Void> finishedBulkRequestCallback,
                                           CardinalBulkRequest bulkRequest) {

        List<CardinalRequestAndResponse> requestsAndResponses = new ArrayList<>();
        Label labelDelta = new Label();
        int throttle = bulkRequest.getThrottle();
        int requestCount = bulkRequest.getRequestCount();
        CardinalRequest request = bulkRequest.getRequest();
        List<String> ids = bulkRequest.getIds();

        task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                if (requestCount > 0) {
                    for (int i = 0; i < requestCount; i++) {
                        final int ii = i;
                        CardinalRequest r = request.withId(String.valueOf(i)).processConstants(getConfig.apply(null));
                        try {
                            Thread.sleep(throttle);
                            CardinalResponse response = sendRequestCallback.apply(r);
                            requestsAndResponses.add(new CardinalRequestAndResponse(r, response));
                        } catch (Exception e) {
                            requestsAndResponses.add(new CardinalRequestAndResponse(r, null));
                        } finally {
                            updateProgress(i + 1, requestCount);
                            Platform.runLater(() -> labelDelta.setText(String.valueOf(ii + 1)));
                        }
                    }
                    finishedBulkRequestCallback.apply(requestsAndResponses, throttle);
                    return true;
                } else {
                    for (int i = 0; i < ids.size(); i++) {
                        final int ii = i;
                        CardinalRequest r = request.withId(ids.get(i)).processConstants(getConfig.apply(null));
                        try {
                            Thread.sleep(throttle);
                            CardinalResponse response = sendRequestCallback.apply(r);
                            requestsAndResponses.add(new CardinalRequestAndResponse(r, response));
                        } catch (Exception e) {
                            requestsAndResponses.add(new CardinalRequestAndResponse(r, null));
                        } finally {
                            updateProgress(i + 1, ids.size());
                            Platform.runLater(() -> labelDelta.setText(String.valueOf(ii + 1)));
                        }
                    }
                    finishedBulkRequestCallback.apply(requestsAndResponses, throttle);
                    return true;
                }
            }
        };

        setHgap(10);
        setVgap(10);
        setPadding(new Insets(20, 60, 20, 60));
        getStyleClass().addAll("plain-border", "round-border");
        getRowConstraints().addAll(
                new RowConstraintsBuilder().withVgrow(Priority.NEVER).build(),
                new RowConstraintsBuilder().withVgrow(Priority.NEVER).build(),
                new RowConstraintsBuilder().withVgrow(Priority.NEVER).build(),
                new RowConstraintsBuilder().withVgrow(Priority.NEVER).build()
        );
        getColumnConstraints().addAll(
                new ColumnConstraintsBuilder().withPercentageWidth(100).build()
        );

        Label labelHeader = new Label("Processing Requests");
        labelHeader.getStyleClass().addAll("header");
        GridPane.setHalignment(labelHeader, HPos.CENTER);
        add(labelHeader, 0, 0);

        ProgressBar progressBar = new ProgressBar();
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(task.progressProperty());
        progressBar.setMaxWidth(java.lang.Double.MAX_VALUE);
        GridPane.setHgrow(progressBar, Priority.ALWAYS);
        add(progressBar, 0, 1);

        GridPane.setHalignment(labelDelta, HPos.CENTER);
        GridPane.setHgrow(labelDelta, Priority.ALWAYS);
        GridPane.setFillWidth(labelDelta, true);
        add(labelDelta, 0, 2);

        Button buttonAbort = new Button("Abort");
        buttonAbort.setOnAction((a) -> {
                task.cancel();
                finishedBulkRequestCallback.apply(requestsAndResponses, throttle);
        });
        GridPane.setHalignment(buttonAbort, HPos.CENTER);
        add(buttonAbort, 0, 3);
    }

    public Task<Boolean> getTask() {
        return task;
    }
}

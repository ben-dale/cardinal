package uk.co.ridentbyte.view.response;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import uk.co.ridentbyte.model.CardinalBulkRequest;
import uk.co.ridentbyte.model.CardinalRequestAndResponse;
import uk.co.ridentbyte.model.CardinalRequest;
import uk.co.ridentbyte.model.CardinalResponse;
import uk.co.ridentbyte.model.Config;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ResponsePane extends BorderPane {

    private Function<Void, Config> getConfig;
    private Function<CardinalRequest, CardinalResponse> sendRequest;
    private Function<List<CardinalRequestAndResponse>, Void> exportToCsv;
    private BiFunction<List<CardinalRequest>, Integer, Void> exportToBash;
    private Function<String, Void> showErrorDialog;
    private Task currentBackgroundTask;

    public ResponsePane(Function<Void, Config> getConfig,
                        Function<CardinalRequest, CardinalResponse> sendRequest,
                        Function<List<CardinalRequestAndResponse>, Void> exportToCsv,
                        BiFunction<List<CardinalRequest>, Integer, Void> exportToBash,
                        Function<String, Void> showErrorDialog) {
        this.getConfig = getConfig;
        this.sendRequest = sendRequest;
        this.exportToCsv = exportToCsv;
        this.exportToBash = exportToBash;
        this.showErrorDialog = showErrorDialog;

        setPadding(new Insets(20, 20, 20, 20));
        this.clear();
    }

    public void clear() {
        stopBulkRequestIfRunning();
        setCenter(new ClearResponsePane());
    }

    public void setResponse(CardinalResponse cardinalResponse) {
        Platform.runLater(() -> {
            clear();
            setCenter(new ResponseOutputPane(cardinalResponse));
        });
    }

    public void loadCurlCommand(String command) {
        Platform.runLater(() -> {
            clear();
            setCenter(new CurlOutputPane(command));
        });
    }

    public void showBulkRequestInput(CardinalRequest request) {
        Platform.runLater(() -> {
            clear();
            setCenter(new BulkRequestInputPane(getConfig, exportToBash, startBulkRequest(), showErrorDialog, request));
        });
    }

    public void stopBulkRequestIfRunning() {
        if (currentBackgroundTask != null) {
            currentBackgroundTask.cancel();
        }
    }

    public java.util.function.Function<CardinalBulkRequest, Void> startBulkRequest() {
        return new java.util.function.Function<CardinalBulkRequest, Void>() {
            @Override
            public Void apply(CardinalBulkRequest bulkRequest) {
                if (bulkRequest.getRequestCount() == 0 && bulkRequest.getIds().isEmpty()) {
                    showErrorDialog.apply("Invalid input. \nPlease provide a throttle and either a request count or a range value.");
                } else {
                    var bulkRequestProcessingOutputPane = new BulkRequestProcessingOutputPane(getConfig, sendRequest, finishedBulkRequestCallback(), bulkRequest);
                    Platform.runLater(() -> setCenter(bulkRequestProcessingOutputPane));
                    currentBackgroundTask = bulkRequestProcessingOutputPane.getTask();
                    var bulkRequestThread = new Thread(currentBackgroundTask);
                    bulkRequestThread.setDaemon(true);
                    bulkRequestThread.start();
                }
                return null;
            }
        };
    }

    public BiFunction<List<CardinalRequestAndResponse>, Integer, Void> finishedBulkRequestCallback() {
        return new java.util.function.BiFunction<List<CardinalRequestAndResponse>, Integer, Void>() {
            @Override
            public Void apply(List<CardinalRequestAndResponse> requestAndResponses, Integer throttle) {
                Platform.runLater(() ->
                        setCenter(new BulkRequestOutputPane(requestAndResponses, exportToCsv, exportToBash, throttle))
                );
                return null;
            }
        };

    }

}

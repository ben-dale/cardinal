package uk.co.ridentbyte.view.response;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import uk.co.ridentbyte.functions.ExportToBash;
import uk.co.ridentbyte.model.CardinalBulkRequest;
import uk.co.ridentbyte.model.CardinalRequest;
import uk.co.ridentbyte.model.Config;
import uk.co.ridentbyte.view.util.ColumnConstraintsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BulkRequestInputPane extends GridPane {

    private TextField textNumOfRequests, textDelay, textForEach;
    private Function<String, Void> showErrorDialogCallback;
    private ExportToBash exportToBash;
    private CardinalRequest baseRequest;
    private Function<Void, Config> getConfig;


    public BulkRequestInputPane(Function<Void, Config> getConfig,
                                ExportToBash exportToBash,
                                Function<CardinalBulkRequest, Void> startBulkRequest,
                                Function<String, Void> showErrorDialogCallback,
                                CardinalRequest request) {

        this.getConfig = getConfig;
        this.baseRequest = request;
        this.exportToBash = exportToBash;
        this.showErrorDialogCallback = showErrorDialogCallback;

        setHgap(10);
        setVgap(10);
        setPadding(new Insets(20));
        getStyleClass().addAll("plain-border", "round-border");

        getColumnConstraints().add(new ColumnConstraintsBuilder().withPercentageWidth(40).withHgrow(Priority.ALWAYS).build());
        getColumnConstraints().add(new ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build());
        getColumnConstraints().add(new ColumnConstraintsBuilder().withHgrow(Priority.NEVER).build());

        Label labelHeader = new Label("Bulk Request");
        labelHeader.getStyleClass().addAll("header");
        GridPane.setColumnSpan(labelHeader, 3);
        GridPane.setHalignment(labelHeader, HPos.CENTER);
        add(labelHeader, 0, 0);

        Label labelDelay = new Label("Delay per request (ms)");
        GridPane.setHalignment(labelDelay, HPos.RIGHT);
        add(labelDelay, 0, 1);

        textDelay = new TextField("200");
        GridPane.setColumnSpan(textDelay, 2);
        add(textDelay, 1, 1);

        Label labelNumOfRequests = new Label("No. of requests");
        GridPane.setHalignment(labelNumOfRequests, HPos.RIGHT);
        add(labelNumOfRequests, 0, 2);

        textNumOfRequests = new TextField();
        GridPane.setColumnSpan(textNumOfRequests, 2);
        add(textNumOfRequests, 1, 2);

        Label labelOr = new Label("- OR -");
        GridPane.setColumnSpan(labelOr, 3);
        GridPane.setHalignment(labelOr, HPos.CENTER);
        add(labelOr, 0, 3);

        Label labelForEach = new Label("For each");
        GridPane.setHalignment(labelForEach, HPos.RIGHT);
        add(labelForEach, 0, 4);

        textForEach = new TextField();
        GridPane.setColumnSpan(textForEach, 2);
        textForEach.setPromptText("325, 454, 432 or 12..54");
        add(textForEach, 1, 4);

        Button buttonStart = new Button("Start");
        GridPane.setHalignment(buttonStart, HPos.RIGHT);
        buttonStart.setOnAction((a) -> startBulkRequest.apply(new CardinalBulkRequest(request, getThrottle(), getNumberOfRequests(), getForEach())));
        add(buttonStart, 2, 5);

        Button buttonExportAsScript = new Button("Export as script...");
        GridPane.setHalignment(buttonExportAsScript, HPos.RIGHT);
        buttonExportAsScript.setOnAction((a) -> asScript());
        add(buttonExportAsScript,1, 5);

    }

    private int getThrottle() {
        try {
            return Integer.parseInt(textDelay.getText().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private int getNumberOfRequests() {
        try {
            return Integer.parseInt(textNumOfRequests.getText().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private List<String> getForEach() {
        String pattern = "([0-9]+)\\.\\.([0-9]+)";
        if (textForEach.getText().trim().matches(pattern)) {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(textForEach.getText().trim());
            if (m.find()) {
                int firstInt = Integer.parseInt(m.group(1));
                int secondInt = Integer.parseInt(m.group(2));
                List<String> ints = new ArrayList<>();
                for (int i = firstInt; i <= secondInt; i++) {
                    ints.add(String.valueOf(i));
                }
                return ints;
            } else {
                return Arrays.asList(textForEach.getText().split(","));
            }
        } else {
            return Arrays.asList(textForEach.getText().split(","));
        }
    }

    private void asScript() {
        int requestCount = getNumberOfRequests();
        List<String> ids = getForEach();
        if (requestCount != 0) {
            List<CardinalRequest> requests = new ArrayList<>();
            for (int i = 0; i < requestCount; i++) {
                requests.add(baseRequest.withId(String.valueOf(i)).processConstants(getConfig.apply(null)));
            }
            exportToBash.export(requests, getThrottle());
        } else if (!ids.isEmpty()) {
            List<CardinalRequest> requests = new ArrayList<>();
            for (String id : ids) {
                requests.add(baseRequest.withId(id).processConstants(getConfig.apply(null)));
            }
            exportToBash.export(requests, getThrottle());
        } else {
            showErrorDialogCallback.apply("Invalid input. \nPlease provide a throttle and either a request count or a range value.");
        }
    }



}

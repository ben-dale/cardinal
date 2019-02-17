package uk.co.ridentbyte.view.response;

import javafx.geometry.Insets;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import uk.co.ridentbyte.model.CardinalRequest;
import uk.co.ridentbyte.model.CardinalRequestAndResponse;
import uk.co.ridentbyte.model.CardinalResponse;
import uk.co.ridentbyte.view.util.RowConstraintsBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BulkRequestOutputPane extends GridPane {

    public BulkRequestOutputPane(List<CardinalRequestAndResponse> requestAndResponses,
                                 Function<List<CardinalRequestAndResponse>, Void> exportToCsv,
                                 BiFunction<List<CardinalRequest>, Integer, Void> exportToBash,
                                 int throttle) {
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(20));
        this.getStyleClass().addAll("plain-border", "round-border");
        this.getRowConstraints().addAll(
                new RowConstraintsBuilder().withPercentageHeight(55).withVgrow(Priority.ALWAYS).build(),
                new RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build(),
                new RowConstraintsBuilder().withVgrow(Priority.NEVER).build()
        );

        List<CardinalResponse> responses = requestAndResponses.stream()
                .map(CardinalRequestAndResponse::getResponse)
                .collect(Collectors.toList());

        List<CardinalRequest> requests = requestAndResponses.stream()
                .map(CardinalRequestAndResponse::getRequest)
                .collect(Collectors.toList());

        NumberAxis xAxis = new NumberAxis();
        xAxis.setForceZeroInRange(true);
        xAxis.setMinorTickVisible(false);
        xAxis.setTickLabelsVisible(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, null, " ms"));
        yAxis.setForceZeroInRange(true);

        XYChart.Series<Number, Number> timeSeries = new XYChart.Series<>();
        for (int i = 0; i < responses.size(); i++) {
            timeSeries.getData().add(new XYChart.Data<>(i, responses.get(i).getTime()));
        }

        AreaChart<Number, Number> lineChart = new AreaChart<>(xAxis, yAxis);
        lineChart.setTitle("Response time over time");
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);
        lineChart.getData().add(timeSeries);
        GridPane.setHgrow(lineChart, Priority.ALWAYS);
        GridPane.setColumnSpan(lineChart, 2);
        add(lineChart, 0, 0);


        List<Long> allResponseTimes = responses.stream()
                .map(CardinalResponse::getTime)
                .collect(Collectors.toList());

        List<Long> allResponseTimesSorted = allResponseTimes.stream().sorted().collect(Collectors.toList());

        long averageResponseTime = 0;
        if (!responses.isEmpty()) {
            averageResponseTime = allResponseTimes.stream().mapToLong(Long::longValue).sum() / responses.size();
        }

        Map<Integer, List<CardinalResponse>> responsesGroupedByStatusCode =
                responses.stream().collect(Collectors.groupingBy(CardinalResponse::getStatusCode));

        StringBuilder timingsOutput = new StringBuilder();
        timingsOutput.append("Timings\n");
        timingsOutput.append("---\n");
        timingsOutput.append("Average Response Time..... ");
        timingsOutput.append(averageResponseTime);
        timingsOutput.append("ms\n");
        timingsOutput.append("Fastest Response Time..... ");
        timingsOutput.append(allResponseTimesSorted.size() > 0 ? allResponseTimesSorted.get(0) : "0");
        timingsOutput.append("ms\n");
        timingsOutput.append("Slowest Response Time..... ");
        timingsOutput.append(allResponseTimesSorted.size() > 0 ? allResponseTimesSorted.get(allResponseTimesSorted.size() - 1) : "0");
        timingsOutput.append("ms\n");
        timingsOutput.append("\n");
        timingsOutput.append("Request/Response Counts\n");
        timingsOutput.append("---\n");
        if (responsesGroupedByStatusCode.isEmpty()) {
            timingsOutput.append("No responses");
        } else {
            for (Map.Entry<Integer, List<CardinalResponse>> statusWithResponses : responsesGroupedByStatusCode.entrySet()) {
                timingsOutput.append("Http ");
                timingsOutput.append(statusWithResponses.getKey());
                timingsOutput.append(".................. ");
                timingsOutput.append(statusWithResponses.getValue().size());
                timingsOutput.append("\n");
            }
        }

        TextArea textAreaTimings = new TextArea(timingsOutput.toString());
        textAreaTimings.getStyleClass().add("cardinal-font-console");
        GridPane.setHgrow(textAreaTimings, Priority.ALWAYS);
        GridPane.setColumnSpan(textAreaTimings, 2);
        add(textAreaTimings, 0, 1);

        Button exportToCsvButton = new Button("Export to CSV...");
        exportToCsvButton.setOnAction((a) -> exportToCsv.apply(requestAndResponses));
        GridPane.setColumnSpan(exportToCsvButton, 1);
        GridPane.setHgrow(exportToCsvButton, Priority.NEVER);
        add(exportToCsvButton, 0, 2);

        Button exportToBashButton = new Button("Export as script...");
        exportToBashButton.setOnAction((a) -> exportToBash.apply(requests, throttle));
        GridPane.setColumnSpan(exportToBashButton, 1);
        GridPane.setHgrow(exportToBashButton, Priority.ALWAYS);
        add(exportToBashButton, 1, 2);

    }

}



package uk.co.ridentbyte.view.dialog;

import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import uk.co.ridentbyte.model.EnvironmentVariable;
import uk.co.ridentbyte.view.util.ColumnConstraintsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnvironmentVariablesEditDialog extends Dialog<List<EnvironmentVariable>> {

    private GridPane grid;
    private List<KeyValueInput> keyValueInputs;
    private Button buttonRemove, buttonAdd;

    public EnvironmentVariablesEditDialog(List<EnvironmentVariable> existingParameters) {
        this.setTitle("Edit Environment Variables");
        this.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        this.grid = new GridPane();
        grid.setGridLinesVisible(true);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPrefWidth(450);
        grid.getColumnConstraints().addAll(
            new ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build(),
            new ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build(),
            new ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build(),
            new ColumnConstraintsBuilder().withHgrow(Priority.NEVER).build()
        );

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefViewportHeight(300);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setPrefViewportWidth(grid.getPrefWidth());
        scrollPane.setContent(grid);
        this.getDialogPane().setContent(scrollPane);

        this.keyValueInputs = new ArrayList<>();
        if (existingParameters.isEmpty()) {
            keyValueInputs.add(new KeyValueInput("", ""));
            keyValueInputs.add(new KeyValueInput("", ""));
            keyValueInputs.add(new KeyValueInput("", ""));
            keyValueInputs.add(new KeyValueInput("", ""));
            keyValueInputs.add(new KeyValueInput("", ""));
        } else {
            for (EnvironmentVariable existingParameter : existingParameters) {
                keyValueInputs.add(new KeyValueInput(existingParameter.key(), existingParameter.value()));
            }

            if (existingParameters.size() < 5) {
                for (int i = 0; i < 5 - existingParameters.size(); i++) {
                    keyValueInputs.add(new KeyValueInput("", ""));
                }
            }
        }
        for (int i = 0; i < keyValueInputs.size(); i++) {
            keyValueInputs.get(i).addTo(grid, i, 0);
        }

        this.buttonRemove = new Button("-");
        buttonRemove.setOnAction((actionEvent) -> {
            keyValueInputs.remove(keyValueInputs.size() - 1);
            render();
            scrollPane.setVvalue(1);
        });
        GridPane.setHalignment(buttonRemove, HPos.RIGHT);

        this.buttonAdd = new Button("+");
        buttonAdd.setOnAction((actionEvent) -> {
            keyValueInputs.add(new KeyValueInput("", ""));
            render();
            scrollPane.setVvalue(1);
        });
        GridPane.setHalignment(buttonAdd, HPos.RIGHT);

        render();

        this.setResultConverter((buttonType) -> {
            if (buttonType == ButtonType.OK) {
                return keyValueInputs.stream()
                        .filter(kvi ->
                                kvi.getKey().trim().length() != 0 || kvi.getValue().trim().length() != 0
                        ).map(kvi ->
                                new EnvironmentVariable(kvi.getKey(), kvi.getValue())
                        ).collect(Collectors.toList());
            } else {
                return null;
            }
        });
    }

    private void render() {
        grid.getChildren().clear();
        for (int i = 0; i < keyValueInputs.size(); i++) {
            keyValueInputs.get(i).addTo(grid, 0, i);
        }
        grid.add(buttonRemove, 2, keyValueInputs.size());
        grid.add(buttonAdd, 3, keyValueInputs.size());
        this.getDialogPane().getScene().getWindow().sizeToScene();
    }

    class KeyValueInput {

        private TextField textKey, textValue;

        public KeyValueInput(String key, String value) {
            this.textKey = new TextField(key);
            textKey.getStyleClass().add("cardinal-font");

            this.textValue = new TextField(value);
            textValue.getStyleClass().add("cardinal-font");

            GridPane.setColumnSpan(textValue, 2);
        }

        public void addTo(GridPane grid, int x, int y) {
            grid.add(textKey, x, y);
            Label labelEquals = new Label("=");
            GridPane.setHalignment(labelEquals, HPos.CENTER);
            grid.add(labelEquals, x + 1, y);
            grid.add(textValue, x + 2, y);
        }

        public String getKey() {
            return this.textKey.getText();
        }

        public String getValue() {
            return this.textValue.getText();
        }

    }

}
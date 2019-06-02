package uk.co.ridentbyte.view.cheatsheet;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import uk.co.ridentbyte.model.RequestString;
import uk.co.ridentbyte.model.Vocabulary;
import uk.co.ridentbyte.view.util.ColumnConstraintsBuilder;
import uk.co.ridentbyte.view.util.RowConstraintsBuilder;

import java.util.List;

public class CheatSheetPane extends GridPane {

    public CheatSheetPane(Vocabulary vocabulary) {
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(20));
        this.getColumnConstraints().setAll(
                new ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build(),
                new ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build()
        );

        this.getRowConstraints().setAll(
                new RowConstraintsBuilder().withVgrow(Priority.NEVER).build(),
                new RowConstraintsBuilder().withVgrow(Priority.NEVER).build(),
                new RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build()
        );

        var labelVariableHeader = new Label("Variables");
        labelVariableHeader.getStyleClass().add("header");
        this.add(labelVariableHeader, 0, 0);

        var labelVariableInfo = new Label("These commands will always return the same value when used multiple times in a single request.");
        labelVariableInfo.getStyleClass().add("cardinal-font");
        labelVariableInfo.setWrapText(true);
        this.add(labelVariableInfo, 0, 1);

        var variableCommands = List.of(
                "#{guid}",
                "#{int}",
                "#{float}",
                "#{firstName}",
                "#{lastName}",
                "#{action}",
                "#{businessEntity}",
                "#{communication}",
                "#{country}",
                "#{object}",
                "#{place}",
                "#{emoji}"
        );

        var variableCommandsScrollPaneGrid = new GridPane();
        variableCommandsScrollPaneGrid.getColumnConstraints().setAll(
                new ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build(),
                new ColumnConstraintsBuilder().withHgrow(Priority.NEVER).build(),
                new ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build()
        );
        variableCommandsScrollPaneGrid.setHgap(10);
        variableCommandsScrollPaneGrid.setVgap(10);
        variableCommandsScrollPaneGrid.setPadding(new Insets(20));

        var variableCommandsScrollPane = new ScrollPane();
        variableCommandsScrollPane.setFitToHeight(true);
        variableCommandsScrollPane.setFitToWidth(true);
        variableCommandsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        variableCommandsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        variableCommandsScrollPane.setContent(variableCommandsScrollPaneGrid);
        this.add(variableCommandsScrollPane, 0, 2);

        for (int i = 0; i < variableCommands.size(); i++) {
            var textCommand = new TextField();
            textCommand.setEditable(false);
            textCommand.getStyleClass().add("cardinal-font");
            textCommand.setText(variableCommands.get(i));
            variableCommandsScrollPaneGrid.add(textCommand, 0, i);

            var labelEquals = new Label("=");
            variableCommandsScrollPaneGrid.add(labelEquals, 1, i);

            var commandResult = new RequestString(variableCommands.get(i), List.of(), vocabulary).process();
            var textCommandResult = new TextField();
            textCommandResult.setEditable(false);
            textCommandResult.getStyleClass().add("cardinal-font");
            textCommandResult.setText(commandResult);
            variableCommandsScrollPaneGrid.add(textCommandResult, 2, i);
        }

        var labelFunctionHeader = new Label("Functions");
        labelFunctionHeader.getStyleClass().add("header");
        this.add(labelFunctionHeader, 1, 0);

        var labelFunctionInfo = new Label("These commands will return a different value when used multiple times in a single request.");
        labelFunctionInfo.getStyleClass().add("cardinal-font");
        labelFunctionInfo.setWrapText(true);
        this.add(labelFunctionInfo, 1, 1);

        var functionCommands = List.of(
                "#{random(\"A\", \"B\", \"C\")}",
                "#{randomBetween(20, 50)}",
                "#{lower(\"HELLO\")}",
                "#{upper(\"hello\")}",
                "#{capitalise(\"hello\")}",
                "#{lorem(4)}",
                "#{randomGuid()}",
                "#{randomInt()}",
                "#{randomFloat()}",
                "#{randomFirstName()}",
                "#{randomLastName()}",
                "#{randomAction()}",
                "#{randomBusinessEntity()}",
                "#{randomCommunication()}",
                "#{randomCountry()}",
                "#{randomObject()}",
                "#{randomPlace()}",
                "#{randomEmoji()}"
        );

        var functionCommandsScrollPaneGrid = new GridPane();
        functionCommandsScrollPaneGrid.getColumnConstraints().setAll(
                new ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build(),
                new ColumnConstraintsBuilder().withHgrow(Priority.NEVER).build(),
                new ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build()
        );
        functionCommandsScrollPaneGrid.setHgap(10);
        functionCommandsScrollPaneGrid.setVgap(10);
        functionCommandsScrollPaneGrid.setPadding(new Insets(20));

        var functionCommandsScrollPane = new ScrollPane();
        functionCommandsScrollPane.setFitToHeight(true);
        functionCommandsScrollPane.setFitToWidth(true);
        functionCommandsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        functionCommandsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        functionCommandsScrollPane.setContent(functionCommandsScrollPaneGrid);
        this.add(functionCommandsScrollPane, 1, 2);

        for (int i = 0; i < functionCommands.size(); i++) {
            var textCommand = new TextField();
            textCommand.setPrefWidth(200);
            textCommand.setEditable(false);
            textCommand.getStyleClass().add("cardinal-font");
            textCommand.setText(functionCommands.get(i));
            functionCommandsScrollPaneGrid.add(textCommand, 0, i);

            var labelEquals = new Label("=");
            functionCommandsScrollPaneGrid.add(labelEquals, 1, i);

            var commandResult = new RequestString(functionCommands.get(i), List.of(), vocabulary).process();
            var textCommandResult = new TextField();
            textCommandResult.setEditable(false);
            textCommandResult.getStyleClass().add("cardinal-font");
            textCommandResult.setText(commandResult);
            functionCommandsScrollPaneGrid.add(textCommandResult, 2, i);
        }

    }

}

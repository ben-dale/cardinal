package uk.co.ridentbyte.view.cheatsheet;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import uk.co.ridentbyte.model.RequestString;
import uk.co.ridentbyte.model.Vocabulary;
import uk.co.ridentbyte.view.util.ColumnConstraintsBuilder;
import uk.co.ridentbyte.view.util.RowConstraintsBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CheatSheetPane extends GridPane {

    public CheatSheetPane(Vocabulary vocabulary) {
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(20));
        this.getColumnConstraints().setAll(
                new ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build()
        );

        this.getRowConstraints().setAll(
                new RowConstraintsBuilder().withVgrow(Priority.ALWAYS).build(),
                new RowConstraintsBuilder().withVgrow(Priority.NEVER).build()
        );
        
        var textCommandsAndOutput = new TextArea();
        textCommandsAndOutput.getStyleClass().addAll("cardinal-font-console", "console-output");
        textCommandsAndOutput.setEditable(false);
        textCommandsAndOutput.setText(generateCheatSheetText(vocabulary));
        this.add(textCommandsAndOutput, 0, 0);

        var buttonRegenerate = new Button("Regenerate");
        buttonRegenerate.setOnAction((e) -> textCommandsAndOutput.setText(generateCheatSheetText(vocabulary)));
        this.add(buttonRegenerate, 0, 1);
    }

    private String generateCheatSheetText(Vocabulary vocabulary) {
        StringBuilder builder = new StringBuilder();

        var commands = List.of(
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
                "#{emoji}",
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
        var commandsSortedByLength = commands
                .stream()
                .sorted(Comparator.comparing(String::length))
                .collect(Collectors.toList());
        var longestCommand = commandsSortedByLength.get(commandsSortedByLength.size() - 1);
        var longestCommandLength = longestCommand.length();

        commands.forEach((command) -> {
            int dotsNeeded = 4 + (longestCommandLength - command.length());
            var commandResult = new RequestString(command, List.of(), vocabulary).process();
            builder.append(command);
            builder.append(".".repeat(dotsNeeded));
            builder.append(commandResult);
            builder.append("\n");
        });

        return builder.toString();

    }
}

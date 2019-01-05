package uk.co.ridentbyte.view.response;

import javafx.geometry.Insets;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import scala.collection.IndexedSeq;
import scala.collection.JavaConverters;
import uk.co.ridentbyte.model.CardinalResponse;
import uk.co.ridentbyte.view.util.ColumnConstraintsBuilder;
import uk.co.ridentbyte.view.util.RowConstraintsBuilder;

import java.util.Map;
import java.util.stream.Collectors;

public class ResponseOutputPane extends GridPane {

    public ResponseOutputPane(CardinalResponse response) {
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(20));
        this.getStyleClass().addAll("plain-border", "round-border");
        this.getColumnConstraints().add(new ColumnConstraintsBuilder().withHgrow(Priority.ALWAYS).build());
        this.getRowConstraints().add(new RowConstraintsBuilder().withVgrow(Priority.ALWAYS).withPercentageHeight(40).build());
        this.getRowConstraints().add(new RowConstraintsBuilder().withVgrow(Priority.ALWAYS).withPercentageHeight(60).build());

        ListView<String> listHeaders = new ListView<String>();
        listHeaders.getStyleClass().add("cardinal-font");
        Map<String, IndexedSeq<String>> headers = JavaConverters.mapAsJavaMap(response.raw().headers());
        for (Map.Entry<String, IndexedSeq<String>> header : headers.entrySet()) {
            String values = JavaConverters.asJavaCollection(header.getValue()).stream().collect(Collectors.joining(""));
            listHeaders.getItems().add(header.getKey() + ": " + values);
        }
        this.add(listHeaders, 0, 0);

        TextArea textAreaBody = new TextArea();
        textAreaBody.setEditable(false);
        textAreaBody.getStyleClass().add("cardinal-font");
        textAreaBody.setText(response.formattedBody());
        this.add(textAreaBody, 0, 1);
    }

}

package uk.co.ridentbyte.view.request;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import uk.co.ridentbyte.model.CardinalRequest;
import uk.co.ridentbyte.view.util.RowConstraintsBuilder;

import java.util.StringJoiner;
import java.util.function.Function;

public class RequestInputPane extends GridPane {

    private RequestUriVerbInputPane uriVerbInputPane;
    private TextArea textHeaders, textAreaBody;

    public RequestInputPane(Function<Void, Void> triggerUnsavedChangesMade) {
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(20));
        this.getRowConstraints().addAll(
            new RowConstraintsBuilder().build(),
            new RowConstraintsBuilder().build(),
            new RowConstraintsBuilder().withMaxHeight(200).build(),
            new RowConstraintsBuilder().build(),
            new RowConstraintsBuilder().build()
        );

        this.uriVerbInputPane = new RequestUriVerbInputPane(triggerUnsavedChangesMade);
        GridPane.setVgrow(uriVerbInputPane, Priority.NEVER);
        GridPane.setHgrow(uriVerbInputPane, Priority.ALWAYS);
        this.add(uriVerbInputPane, 0, 0);

        Label labelHeaders = new Label("Headers");
        labelHeaders.getStyleClass().add("cardinal-font");
        GridPane.setVgrow(labelHeaders, Priority.NEVER);
        GridPane.setHgrow(labelHeaders, Priority.ALWAYS);
        this.add(labelHeaders, 0, 1);

        this.textHeaders = new TextArea();
        this.textHeaders.textProperty().addListener ( (arg, oldVal, newVal) -> triggerUnsavedChangesMade.apply(null));
        this.textHeaders.getStyleClass().add("cardinal-font");
        GridPane.setVgrow(this.textHeaders, Priority.ALWAYS);
        GridPane.setHgrow(this.textHeaders, Priority.ALWAYS);
        this.add(textHeaders, 0, 2);

        Label labelBody = new Label("Body");
        labelBody.getStyleClass().add("cardinal-font");
        GridPane.setVgrow(labelBody, Priority.NEVER);
        GridPane.setHgrow(labelBody, Priority.ALWAYS);
        this.add(labelBody, 0, 3);

        this.textAreaBody = new TextArea();
        this.textAreaBody.textProperty().addListener ( (arg, oldVal, newVal) -> triggerUnsavedChangesMade.apply(null));
        this.textAreaBody.getStyleClass().add("cardinal-font");
        GridPane.setVgrow(this.textAreaBody, Priority.ALWAYS);
        GridPane.setHgrow(this.textAreaBody, Priority.ALWAYS);
        this.add(textAreaBody, 0, 4);
    }

    private String[] getHeaders() {
        if (this.textHeaders.getText().trim().isEmpty()) {
            return new String[]{};
        } else {
            return this.textHeaders.getText().split("\n");
        }
    }

    private String getBody() {
        return this.textAreaBody.getText().trim();
    }

    public void setBody(String body) {
        if (body != null) {
            this.textAreaBody.setText(body);
        }
    }

    public CardinalRequest getRequest() {
        String body = this.getBody();
        String[] headers = this.getHeaders();
        String verb = this.uriVerbInputPane.getVerb();
        String uri = this.uriVerbInputPane.getUri();
        var followRedirects = this.uriVerbInputPane.shouldFollowRedirects();
        return new CardinalRequest(uri, verb, headers, body, followRedirects);
    }

    public void clear() {
        this.uriVerbInputPane.clear();
        this.textHeaders.clear();
        this.textAreaBody.clear();
    }

    public void loadRequest(CardinalRequest request) {
        StringJoiner headers = new StringJoiner("\n");
        for (String header : request.getHeaders()) {
            headers.add(header);
        }

        this.setBody(request.getBody());
        this.textHeaders.setText(headers.toString());
        this.uriVerbInputPane.setVerb(request.getVerb());
        this.uriVerbInputPane.setUri(request.getUri());
    }

    public void addHeader(String header) {
        if (this.textHeaders.getText().trim().isEmpty()) {
            this.textHeaders.setText(header);
        } else {
            this.textHeaders.setText(this.textHeaders.getText() + "\n" + header);
        }
    }

}

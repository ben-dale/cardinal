package uk.co.ridentbyte.view.request;

import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import uk.co.ridentbyte.functions.UnsavedChangesMade;

public class RequestUriVerbInputPane extends GridPane {

    private TextField textUri;
    private ChoiceBox<String> selectVerb;
    private CheckBox followRedirects;

    public RequestUriVerbInputPane(UnsavedChangesMade unsavedChangesMade) {
        this.setHgap(10);
        this.setVgap(10);

        this.textUri = new TextField();
        this.textUri.getStyleClass().add("cardinal-font");
        this.textUri.textProperty().addListener((arg, oldVal, newVal) -> unsavedChangesMade.trigger());
        this.textUri.setPromptText("http://localhost:8080");
        GridPane.setVgrow(this.textUri, Priority.NEVER);
        GridPane.setHgrow(this.textUri, Priority.ALWAYS);
        this.add(this.textUri, 0, 0);

        this.selectVerb = new ChoiceBox<>(FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "TRACE", "PATCH"));
        this.selectVerb.getStyleClass().add("cardinal-font");
        this.selectVerb.getSelectionModel().selectFirst();
        this.selectVerb.getSelectionModel().selectedItemProperty().addListener((arg, oldVal, newVal) -> unsavedChangesMade.trigger());
        GridPane.setVgrow(this.selectVerb, Priority.NEVER);
        GridPane.setHgrow(this.selectVerb, Priority.NEVER);
        add(this.selectVerb, 1, 0);

        this.followRedirects = new CheckBox("Follow redirects");
        this.followRedirects.selectedProperty().addListener((arg) -> unsavedChangesMade.trigger());
        add(this.followRedirects, 0, 1);
    }

    public String getUri() {
        return this.textUri.getText().trim();
    }

    public void setUri(String uri) {
        this.textUri.setText(uri);
    }

    public String getVerb() {
        return this.selectVerb.getSelectionModel().getSelectedItem();
    }

    public boolean shouldFollowRedirects() {
        return this.followRedirects.isSelected();
    }

    public void setVerb(String verb) {
        int index = this.selectVerb.getItems().indexOf(verb);
        if (index >= 0) {
            this.selectVerb.getSelectionModel().select(index);
        }
    }

    public void clear() {
        this.textUri.setText("");
        this.selectVerb.getSelectionModel().select(0);
    }

    public void setShouldFollowRedirects(boolean shouldFollowRedirects) {
        this.followRedirects.setSelected(shouldFollowRedirects);
    }
}

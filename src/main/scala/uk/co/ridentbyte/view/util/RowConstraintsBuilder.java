package uk.co.ridentbyte.view.util;

import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

public class RowConstraintsBuilder {

    private RowConstraints rowConstraints;

    public RowConstraintsBuilder() {
        this.rowConstraints = new RowConstraints();
    }

    public RowConstraintsBuilder withVgrow(Priority priority) {
        this.rowConstraints.setVgrow(priority);
        return this;
    }

    public RowConstraintsBuilder withMaxHeight(int maxHeight) {
        this.rowConstraints.setMaxHeight(maxHeight);
        return this;
    }

    public RowConstraintsBuilder withPercentageHeight(int percentage) {
        this.rowConstraints.setPercentHeight(percentage);
        return this;
    }

    public RowConstraints build() {
        return this.rowConstraints;
    }
}

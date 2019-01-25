package uk.co.ridentbyte.view.util;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;

public class ColumnConstraintsBuilder {

    private ColumnConstraints columnConstraints;

    public ColumnConstraintsBuilder() {
        this.columnConstraints = new ColumnConstraints();
    }

    public ColumnConstraintsBuilder withHgrow(Priority priority) {
        columnConstraints.setHgrow(priority);
        return this;
    }

    public ColumnConstraintsBuilder withPercentageWidth(int percentage) {
        columnConstraints.setPercentWidth(percentage);
        return this;
    }

    public ColumnConstraints build() {
        return columnConstraints;
    }
}

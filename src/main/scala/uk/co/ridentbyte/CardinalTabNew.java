package uk.co.ridentbyte;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import uk.co.ridentbyte.view.CardinalView;

import java.io.File;
import java.util.function.Function;

public class CardinalTabNew extends Tab {

    private File currentFile;
    private boolean unsavedChanges;

    public CardinalTabNew(File currentFile,
                          CardinalView content,
                          Function<Void, Void> openNewFileIfNoneOpen,
                          QuadFunction<String, Function<Void, Void>, Function<Void, Void>, Function<Void, Void>, Void> showConfirmDialog,
                          Function<Void, Void> save) {
        super(currentFile == null ? "Untitled" : currentFile.getName(), content);
        getStyleClass().add("cardinal-font");
        setOnCloseRequest((e) -> {
           if (unsavedChanges) {
               showConfirmDialog.apply(
                       "Save unsaved changes?",
                       (v) -> save.apply(null),
                       (v) -> null,
                       (v) -> {e.consume(); return null;}
               );
           }
        });
        Platform.runLater(() -> openNewFileIfNoneOpen.apply(null));

        this.currentFile = currentFile;
        this.unsavedChanges = false;
    }

    public void handleUnsavedChangesMade() {
        if (!getText().endsWith("*")) {
            unsavedChanges = true;
            setText(getText() + "*");
        }
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
        setText(currentFile.getName());
    }

    public boolean hasUnsavedChanges() {
        return unsavedChanges;
    }

    public void setUnsavedChanges(boolean unsavedChanges) {
        if (!unsavedChanges) {
            setText(currentFile == null ? "Untitled" : currentFile.getName());
        }
        this.unsavedChanges = unsavedChanges;
    }

    public File getCurrentFile() {
        return currentFile;
    }

}
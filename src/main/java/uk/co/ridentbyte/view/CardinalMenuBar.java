package uk.co.ridentbyte.view;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import uk.co.ridentbyte.functions.NewTab;
import uk.co.ridentbyte.functions.OpenFile;
import uk.co.ridentbyte.functions.ShowBasicAuthInput;

import java.util.function.Function;

public class CardinalMenuBar extends MenuBar {

    public CardinalMenuBar(NewTab newTab,
                           OpenFile openFile,
                           Function<Void, Void> save,
                           Function<Void, Void> saveAs,
                           Function<Void, Void> editEnvVars,
                           Function<Void, Void> showFormUrlEncodeDialog,
                           ShowBasicAuthInput showBasicAuthDialog) {

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            setUseSystemMenuBar(true);
        }

        // *** FILE ***
        Menu menuFile = new Menu("File");

        MenuItem menuItemNew = new MenuItem("New");
        menuItemNew.setOnAction((actionEvent) -> newTab.spawn());
        menuFile.getItems().add(menuItemNew);

        MenuItem menuItemOpen = new MenuItem("Open...");
        menuItemOpen.setOnAction((actionEvent) -> openFile.trigger());
        menuFile.getItems().add(menuItemOpen);

        MenuItem menuItemSave = new MenuItem("Save");
        menuItemSave.setOnAction((actionEvent) -> save.apply(null));
        menuFile.getItems().add(menuItemSave);

        MenuItem menuItemSaveAs = new MenuItem("Save As...");
        menuItemSaveAs.setOnAction((actionEvent) -> saveAs.apply(null));
        menuFile.getItems().add(menuItemSaveAs);

        getMenus().add(menuFile);

        // *** AUTHORISATION ***
        Menu menuAuth = new Menu("Authorisation");

        MenuItem menuItemBasicAuth = new MenuItem("Basic Authorisation...");
        menuItemBasicAuth.setOnAction((actionEvent) -> showBasicAuthDialog.show());
        menuAuth.getItems().add(menuItemBasicAuth);

        getMenus().add(menuAuth);

        // *** FORM ***
        Menu menuForm = new Menu("Form");

        MenuItem menuItemFormUrlEncoded = new MenuItem("URL Encoded...");
        menuItemFormUrlEncoded.setOnAction((actionEvent) -> showFormUrlEncodeDialog.apply(null));
        menuForm.getItems().add(menuItemFormUrlEncoded);

        getMenus().add(menuForm);

        // *** CONFIG ***
        Menu menuConfig = new Menu("Configuration");

        MenuItem menuItemEnvVars = new MenuItem("Edit Environment Variables...");
        menuItemEnvVars.setOnAction((actionEvent) -> editEnvVars.apply(null));
        menuConfig.getItems().add(menuItemEnvVars);

        getMenus().add(menuConfig);
    }

}

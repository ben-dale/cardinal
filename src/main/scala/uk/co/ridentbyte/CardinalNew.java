package uk.co.ridentbyte;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import uk.co.ridentbyte.model.Config;
import uk.co.ridentbyte.model.EnvironmentVariable;
import uk.co.ridentbyte.model.Vocabulary;
import uk.co.ridentbyte.model.Words;
import uk.co.ridentbyte.view.CardinalView;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CardinalNew extends Application  {

    private static Words firstNames, lastNames, countries,
            objects, actions, businessEntities, communications, places, loremipsum, emoji;

    public static Vocabulary vocabulary;

    private static String configLocation = System.getProperty("user.home") + "/.cardinal_config.json";
    private static Config currentConfig = null;
    private static Stage currentStage = null;
    private static TabPane cardinalTabs = null;

    private static Tab newRequestTab = new Tab("+");



    public static void main() throws Exception {
        firstNames = new Words(Files.readAllLines(Paths.get("firstName.txt")), new Random());
        lastNames = new Words(Files.readAllLines(Paths.get("lastNames.txt")), new Random());
        countries = new Words(Files.readAllLines(Paths.get("countries.txt")), new Random());
        objects = new Words(Files.readAllLines(Paths.get("objects.txt")), new Random());
        actions = new Words(Files.readAllLines(Paths.get("actions.txt")), new Random());
        businessEntities = new Words(Files.readAllLines(Paths.get("businessEntities.txt")), new Random());
        communications = new Words(Files.readAllLines(Paths.get("communications.txt")), new Random());
        places = new Words(Files.readAllLines(Paths.get("places.txt")), new Random());
        loremipsum = new Words(Files.readAllLines(Paths.get("loremipsum.txt")), new Random());
        emoji = new Words(Files.readAllLines(Paths.get("emoji.txt")), new Random());
        vocabulary = new Vocabulary(firstNames, lastNames, places, objects, actions, countries, communications, businessEntities, loremipsum, emoji);

        newRequestTab.setClosable(false);
        newRequestTab.setOnSelectionChanged((e) -> {
            if ((e.getSource()) == newRequestTab) {
//                newTab.apply(null);
            }
        });
        cardinalTabs.getTabs().add(newRequestTab);

        Application.launch(CardinalNew.class);
    }

    private void newTab() {
//        cardinalTabs.getTabs().add(
//          cardinalTabs.getTabs().size() - 1,
//        CardinalTab(None, new CardinalView(showAsCurl, showErrorDialog, getCurrentConfig, exportToCsv, exportToBash, sendRequest, triggerUnsavedChangesMade))

//        );
        cardinalTabs.getSelectionModel().select(cardinalTabs.getTabs().size() - 2);

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        currentStage = primaryStage;
        primaryStage.setTitle("Cardinal");
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(800);

        BorderPane view = new BorderPane();
//        view.setTop(menuBar);
        view.setCenter(cardinalTabs);

        Scene scene = new Scene(view, 1000, 500);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());

        Font.loadFont(getClass().getClassLoader().getResource("OpenSans-Regular.ttf").toExternalForm(), 13);

        File file = new File(configLocation);
        if (file.exists() && !file.isDirectory()) {
            String lines = String.join("", Files.readAllLines(Paths.get(file.getPath())));
            currentConfig = new Config(lines);
        } else {
            // Create new empty config file if not present
            Config conf = new Config(new ArrayList<>());
//            saveChangesToConfig.apply(conf);
            currentConfig = conf;
        }


        primaryStage.show();
    }

    public void writeToFile(File file, String data) throws Exception {
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(data);
        fileWriter.close();
    }
}

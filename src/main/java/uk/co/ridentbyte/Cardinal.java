package uk.co.ridentbyte;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import uk.co.ridentbyte.functions.QuadFunction;
import uk.co.ridentbyte.model.BashScript;
import uk.co.ridentbyte.model.BasicAuth;
import uk.co.ridentbyte.model.CardinalHttpResponse;
import uk.co.ridentbyte.model.CardinalRequest;
import uk.co.ridentbyte.model.CardinalRequestAndResponse;
import uk.co.ridentbyte.model.CardinalResponse;
import uk.co.ridentbyte.model.Config;
import uk.co.ridentbyte.model.EnvironmentVariable;
import uk.co.ridentbyte.model.FormUrlEncoded;
import uk.co.ridentbyte.model.Http;
import uk.co.ridentbyte.model.Vocabulary;
import uk.co.ridentbyte.model.Words;
import uk.co.ridentbyte.view.CardinalMenuBar;
import uk.co.ridentbyte.view.CardinalTab;
import uk.co.ridentbyte.view.CardinalView;
import uk.co.ridentbyte.view.cheatsheet.CheatSheetPane;
import uk.co.ridentbyte.view.dialog.BasicAuthInputDialog;
import uk.co.ridentbyte.view.dialog.EnvironmentVariablesEditDialog;
import uk.co.ridentbyte.view.dialog.FormUrlEncodedInputDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Cardinal extends Application  {

    public Vocabulary vocabulary;
    private String configLocation = System.getProperty("user.home") + "/.cardinal_config.json";
    private Config currentConfig = null;
    private Stage currentStage = null;
    private TabPane cardinalTabs = null;

    public static void main(String[] args) {
        launch();
    }

    private List<String> readLinesFrom(String filename) {
        return new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(filename)),
                        StandardCharsets.UTF_8
                )
        ).lines().collect(Collectors.toList());
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Words firstNames = new Words(readLinesFrom("firstNames.txt"), new Random());
        Words lastNames = new Words(readLinesFrom("lastNames.txt"), new Random());
        Words countries = new Words(readLinesFrom("countries.txt"), new Random());
        Words objects = new Words(readLinesFrom("objects.txt"), new Random());
        Words actions = new Words(readLinesFrom("actions.txt"), new Random());
        Words businessEntities = new Words(readLinesFrom("businessEntities.txt"), new Random());
        Words communications = new Words(readLinesFrom("communications.txt"), new Random());
        Words places = new Words(readLinesFrom("places.txt"), new Random());
        Words loremipsum = new Words(readLinesFrom("loremipsum.txt"), new Random());
        Words emoji = new Words(readLinesFrom("emoji.txt"), new Random());
        vocabulary = new Vocabulary(firstNames, lastNames, places, objects, actions, countries, communications, businessEntities, loremipsum, emoji);

        CardinalMenuBar menuBar = new CardinalMenuBar(this::newTab, this::openFile, save(), saveAs(), showEnvironmentVariablesInput(), showFormUrlEncodedInput(), this::showBasicAuthInput);

        var newRequestTab = new Tab("+");
        newRequestTab.setClosable(false);
        newRequestTab.setOnSelectionChanged((e) -> {
            if ((e.getSource()) == newRequestTab) {
                newTab();
            }
        });

        cardinalTabs = new TabPane();

        var cheatSheetTab = new Tab("Cheat Sheet", new CheatSheetPane(vocabulary));
        cheatSheetTab.setClosable(false);
        cardinalTabs.getTabs().add(cheatSheetTab);
        cardinalTabs.getTabs().add(newRequestTab);
        newTab();

        currentStage = primaryStage;
        primaryStage.setTitle("Cardinal");
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(800);

        BorderPane view = new BorderPane();
        view.setTop(menuBar);
        view.setCenter(cardinalTabs);

        Scene scene = new Scene(view, 1000, 500);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("style.css")).toExternalForm());

        Font.loadFont(Objects.requireNonNull(getClass().getClassLoader().getResource("OpenSans-Regular.ttf")).toExternalForm(), 13);
        Font.loadFont(Objects.requireNonNull(getClass().getClassLoader().getResource("RobotoMono-Regular.ttf")).toExternalForm(), 13);

        File file = new File(configLocation);
        if (file.exists() && !file.isDirectory()) {
            String lines = String.join("", Files.readAllLines(Paths.get(file.getPath())));
            currentConfig = new Config(lines);
        } else {
            // Create new empty config file if not present
            Config conf = new Config(new ArrayList<>());
            saveChangesToConfig(conf);
            currentConfig = conf;
        }

        scene.addEventFilter(KeyEvent.KEY_PRESSED, (keyEvent) -> {
            KeyCodeCombination saveAsCombo = new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN);
            KeyCodeCombination saveCombo = new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN);
            KeyCodeCombination closeTabCombo = new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN);
            KeyCodeCombination openCombo = new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN);
            KeyCodeCombination newCombo = new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN);
            KeyCodeCombination sendCombo = new KeyCodeCombination(KeyCode.ENTER, KeyCodeCombination.SHORTCUT_DOWN);
            KeyCodeCombination exitCombo = new KeyCodeCombination(KeyCode.Q, KeyCodeCombination.SHORTCUT_DOWN);

            if (saveCombo.match(keyEvent)) {
                save().apply(null);
            } else if (saveAsCombo.match(keyEvent)) {
                saveAs().apply(null);
            } else if (closeTabCombo.match(keyEvent) && getCurrentTab() != null) {
                CardinalTab currentTab = getCurrentTab();
                Function<Void, Void> remove = (v) -> {
                    ((CardinalView) currentTab.getContent()).clearAll();
                    cardinalTabs.getTabs().remove(currentTab);
                    return null;
                };

                if (currentTab.hasUnsavedChanges()) {
                    showConfirmDialog().apply("Save unsaved changes?", save(), remove, (v) -> null);
                } else {
                    remove.apply(null);
                }
            } else if (openCombo.match(keyEvent)) {
                openFile();
            } else if (newCombo.match(keyEvent)) {
                newTab();
            } else if (sendCombo.match(keyEvent)) {
                ((CardinalView) getCurrentTab().getContent()).triggerSendRequest();
            } else if (exitCombo.match(keyEvent)) {
                primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
            }
        });

        // Temporary action for dev
//    scene.setOnKeyPressed(k => {
//      if (k.getCode == KeyCode.R) {
//        if (currentStage != null) {
//          currentStage.getScene.getStylesheets.clear()
//          println("[" + System.currentTimeMillis() + "] Reloading CSS")
//          val f = new File("src/main/resources/style.css")
//          currentStage.getScene.getStylesheets.add("file://" + f.getAbsolutePath)
//        }
//      }
//    })


        primaryStage.setScene(scene);
        primaryStage.show();

        scene.getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, (e) -> {
            try {
                CardinalTab currentTab = getCurrentTab();
                if (currentTab.hasUnsavedChanges()) {
                    showConfirmDialog().apply("Save unsaved changes?", save(), (v) -> null, (v) -> {
                        e.consume();
                        return null;
                    });
                }
            } catch (ClassCastException cce) {
                // TODO clean this up! This is thrown when Cheat Sheet is active and window is closed
            }
        });
    }

    private void newTab() {
        var cardinalView = new CardinalView(
                this::showAsCurl,
                this::showErrorDialog,
                getCurrentConfig(),
                this::exportToCsv,
                this::exportToBash,
                sendRequest(),
                this::triggerUnsavedChangesMade,
                vocabulary
        );
        var cardinalTab = new CardinalTab(
                null,
                cardinalView,
                showConfirmDialog(),
                save()
        );
        cardinalTabs.getTabs().add(cardinalTabs.getTabs().size() - 1, cardinalTab);
        cardinalTabs.getSelectionModel().select(cardinalTabs.getTabs().size() - 2);
    }

    private void showAsCurl() {
        CardinalTab currentTab = getCurrentTab();
        if (currentTab != null) {
            CardinalRequest request = ((CardinalView) currentTab.getContent()).getRequest();
            if (request.getUri().trim().length() == 0) {
                showErrorDialog("Please enter a URL.");
            } else {
                ((CardinalView) currentTab.getContent()).loadCurlCommand(request.toCurl(currentConfig));
            }
        }
    }

    private void showErrorDialog(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void writeToFile(File file, String data) {
        try (var fileWriter = new FileWriter(file)) {
            fileWriter.write(data);
        } catch (Exception e) {
            // TODO?
        }
    }

    private CardinalTab getCurrentTab() {
        return ((CardinalTab) cardinalTabs.getSelectionModel().getSelectedItem());
    }

    private Function<Void, Config> getCurrentConfig() {
        return (v) -> currentConfig;
    }

    private void exportToCsv(List<CardinalRequestAndResponse> requestAndResponses) {
        String header = CardinalRequest.csvHeaders + "," + CardinalResponse.csvHeaders();
        String content = requestAndResponses.stream().map((reqAndRes) ->
                reqAndRes.getRequest().toCsv() + "," +
                        (reqAndRes.getResponse() != null ?
                                reqAndRes.getResponse().toCSV() :
                                CardinalResponse.blank().toCSV()
                        )).collect(Collectors.joining("\n"));
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showSaveDialog(currentStage);
        if (file != null) {
            File fileWithExtension;
            if (!file.getAbsolutePath().endsWith(".csv")) {
                fileWithExtension = new File(file.getAbsolutePath() + ".csv");
            } else {
                fileWithExtension = file;
            }
            writeToFile(fileWithExtension, header + "\n" + content);
        }
    }

    private void exportToBash(List<CardinalRequest> requests, int throttle) {
        BashScript script = new BashScript(requests, currentConfig, throttle, LocalDateTime.now());
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showSaveDialog(currentStage);
        if (file != null) {
            try {
                script.writeTo(file);
            } catch (IOException ioe) {
                showErrorDialog("Unable to write bash script to a file.");
            }
        }
    }

    private Function<CardinalRequest, CardinalResponse> sendRequest() {
        return (request) -> {
            long startTime = System.currentTimeMillis();
            CardinalHttpResponse httpResponse = new Http(request).send();
            long totalTime = System.currentTimeMillis() - startTime;
            return new CardinalResponse(httpResponse, totalTime);
        };
    }

    private void triggerUnsavedChangesMade() {
        getCurrentTab().handleUnsavedChangesMade();
    }

    private Function<Void, Void> save() {
        return (v) -> {
            CardinalTab currentTab = getCurrentTab();
            if (currentTab != null && currentTab.getCurrentFile() != null) {
                writeToFile(
                        currentTab.getCurrentFile(),
                        ((CardinalView) currentTab.getContent()).getRequest().toJson()
                );
                currentTab.setUnsavedChanges(false);
            } else {
                saveAs().apply(null);
            }
            return null;
        };
    }

    private QuadFunction<String, Function<Void, Void>, Function<Void, Void>, Function<Void, Void>, Void> showConfirmDialog() {
        return (message, onYes, onNo, onCancel) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Save Changes");
            alert.setContentText(message + "\n\n");

            ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.RIGHT);
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.RIGHT);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.LEFT);
            alert.getButtonTypes().setAll(cancelButton, noButton, yesButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                switch (result.get().getText()) {
                    case "Yes":
                        onYes.apply(null);
                        break;
                    case "No":
                        onNo.apply(null);
                        break;
                    case "Cancel":
                        onCancel.apply(null);
                        break;
                }
            }
            return null;
        };
    }

    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(currentStage);
        if (selectedFiles != null) {
            selectedFiles.forEach((file) -> {
                try {
                        String lines = String.join("", Files.readAllLines(file.toPath()));
                        CardinalView cardinalView = new CardinalView(
                        this::showAsCurl,
                        this::showErrorDialog,
                        getCurrentConfig(),
                        this::exportToCsv,
                        this::exportToBash,
                        sendRequest(),
                        this::triggerUnsavedChangesMade,
                        vocabulary
                    );
                    addTab(new CardinalTab(file, cardinalView, showConfirmDialog(), save()));
                    cardinalView.loadRequest(CardinalRequest.apply(lines));
                    getCurrentTab().setUnsavedChanges(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void showBasicAuthInput() {
        CardinalTab currentTab = getCurrentTab();
        if (currentTab != null) {
            BasicAuthInputDialog dialog = new BasicAuthInputDialog();
            Optional<BasicAuth> result = dialog.showAndWait();
            result.ifPresent(basicAuth -> ((CardinalView) currentTab.getContent()).addHeader(basicAuth.asAuthHeader()));
        }
    }

    private Function<Void, Void> showFormUrlEncodedInput() {
        return (v) -> {
            CardinalTab currentTab = getCurrentTab();
            if (currentTab != null) {
                FormUrlEncodedInputDialog dialog = new FormUrlEncodedInputDialog(
                        ((CardinalView) currentTab.getContent()).getRequest().getBody()
                );
                Optional<FormUrlEncoded> result = dialog.showAndWait();
                if (result.isPresent()) {
                    ((CardinalView)currentTab.getContent()).setBody(result.get().toString());
                    ((CardinalView)currentTab.getContent()).addHeader(result.get().header());
                }
            }
            return null;
        };
    }

    private Function<Void, Void> showEnvironmentVariablesInput() {
        return (v) -> {
            EnvironmentVariablesEditDialog dialog = new EnvironmentVariablesEditDialog(currentConfig.getEnvironmentVariables());
            Optional<List<EnvironmentVariable>> result = dialog.showAndWait();
            result.ifPresent(this::setEnvironmentVariables);
            return null;
        };
    }

    private Function<Void, Void> saveAs() {
        return (v) -> {
          CardinalTab currentTab = getCurrentTab();
          if (currentTab != null) {
              FileChooser fileChooser = new FileChooser();
              File file = fileChooser.showSaveDialog(currentStage);
              if (file != null) {
                  File fileWithExtension = null;
                  if (!file.getAbsolutePath().endsWith(".json")) {
                      fileWithExtension = new File(file.getAbsolutePath() + ".json");
                  } else {
                      fileWithExtension = file;
                  }

                  if (currentTab.getCurrentFile() == null) {
                      // New file so just save file
                      writeToFile(fileWithExtension, ((CardinalView) currentTab.getContent()).getRequest().toJson());
                      currentTab.setCurrentFile(fileWithExtension);
                      currentTab.setUnsavedChanges(false);
                  } else {
                      // Existing file so save and open in new tab
                      CardinalRequest request = ((CardinalView) currentTab.getContent()).getRequest();
                      writeToFile(fileWithExtension, request.toJson());
                      CardinalView cardinalView = new CardinalView(
                              this::showAsCurl,
                              this::showErrorDialog,
                              getCurrentConfig(),
                              this::exportToCsv,
                              this::exportToBash,
                              sendRequest(),
                              this::triggerUnsavedChangesMade,
                              vocabulary
                      );
                      addTab(new CardinalTab(fileWithExtension, cardinalView, showConfirmDialog(), save()));
                      cardinalView.loadRequest(request);
                      getCurrentTab().setUnsavedChanges(false);
                  }
              }
          }
          return null;
        };
    }

    private void setEnvironmentVariables(List<EnvironmentVariable> environmentVariables) {
        currentConfig = currentConfig.withEnvironmentVariables(environmentVariables);
        saveChangesToConfig(currentConfig);
    }

    private void saveChangesToConfig(Config config) {
        File file = new File(configLocation);
        writeToFile(file, config.toJson());
    }

    private void addTab(Tab tab) {
        cardinalTabs.getTabs().add(cardinalTabs.getTabs().size() - 1, tab);
        cardinalTabs.getSelectionModel().select(cardinalTabs.getTabs().size() - 2);
    }

}
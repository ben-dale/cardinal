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
import org.apache.commons.io.IOUtils;
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
import uk.co.ridentbyte.view.CardinalView;
import uk.co.ridentbyte.view.cheatsheet.CheatSheetPane;
import uk.co.ridentbyte.view.dialog.BasicAuthInputDialog;
import uk.co.ridentbyte.view.dialog.EnvironmentVariablesEditDialog;
import uk.co.ridentbyte.view.dialog.FormUrlEncodedInputDialog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Cardinal extends Application  {

    private Words firstNames, lastNames, countries,
            objects, actions, businessEntities, communications, places, loremipsum, emoji;

    public static Vocabulary vocabulary;

    private String configLocation = System.getProperty("user.home") + "/.cardinal_config.json";
    private Config currentConfig = null;
    private Stage currentStage = null;
    private TabPane cardinalTabs = null;
    private CardinalMenuBar menuBar = null;

    public static void main(String[] args) {
        launch();
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        firstNames = new Words(IOUtils.readLines(getClass().getClassLoader().getResourceAsStream("firstNames.txt"), StandardCharsets.UTF_8), new Random());
        lastNames = new Words(IOUtils.readLines(getClass().getClassLoader().getResourceAsStream("lastNames.txt"), StandardCharsets.UTF_8), new Random());
        countries = new Words(IOUtils.readLines(getClass().getClassLoader().getResourceAsStream("countries.txt"), StandardCharsets.UTF_8), new Random());
        objects = new Words(IOUtils.readLines(getClass().getClassLoader().getResourceAsStream("objects.txt"), StandardCharsets.UTF_8), new Random());
        actions = new Words(IOUtils.readLines(getClass().getClassLoader().getResourceAsStream("actions.txt"), StandardCharsets.UTF_8), new Random());
        businessEntities = new Words(IOUtils.readLines(getClass().getClassLoader().getResourceAsStream("businessEntities.txt"), StandardCharsets.UTF_8), new Random());
        communications = new Words(IOUtils.readLines(getClass().getClassLoader().getResourceAsStream("communications.txt"), StandardCharsets.UTF_8), new Random());
        places = new Words(IOUtils.readLines(getClass().getClassLoader().getResourceAsStream("places.txt"), StandardCharsets.UTF_8), new Random());
        loremipsum = new Words(IOUtils.readLines(getClass().getClassLoader().getResourceAsStream("loremipsum.txt"), StandardCharsets.UTF_8), new Random());
        emoji = new Words(IOUtils.readLines(getClass().getClassLoader().getResourceAsStream("emoji.txt"), StandardCharsets.UTF_8), new Random());
        vocabulary = new Vocabulary(firstNames, lastNames, places, objects, actions, countries, communications, businessEntities, loremipsum, emoji);

        menuBar = new CardinalMenuBar(newTab(), open(), save(), saveAs(), clearAll(), showEnvironmentVariablesInput(), showFormUrlEncodedInput(), showBasicAuthInput());

        var newRequestTab = new Tab("+");
        newRequestTab.setClosable(false);
        newRequestTab.setOnSelectionChanged((e) -> {
            if ((e.getSource()) == newRequestTab) {
                newTab().apply(null);
            }
        });

        cardinalTabs = new TabPane();

        var cheatSheetTab = new Tab("Cheat Sheet", new CheatSheetPane(vocabulary));
        cheatSheetTab.setClosable(false);
        cardinalTabs.getTabs().add(cheatSheetTab);
        cardinalTabs.getTabs().add(newRequestTab);
        newTab().apply(null);

        currentStage = primaryStage;
        primaryStage.setTitle("Cardinal");
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(800);

        BorderPane view = new BorderPane();
        view.setTop(menuBar);
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
                    cardinalTabs.getTabs().remove(currentTab);
                    openNewFileIfNoneOpen().apply(null);
                    return null;
                };

                if (currentTab.hasUnsavedChanges()) {
                    showConfirmDialog().apply("Save unsaved changes?", save(), remove, (v) -> null);
                } else {
                    remove.apply(null);
                }
            } else if (openCombo.match(keyEvent)) {
                open().apply(null);
            } else if (newCombo.match(keyEvent)) {
                newTab().apply(null);
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

    private Function<Void, Void> newTab() {
        return (v) -> {
            cardinalTabs.getTabs().add(
                    cardinalTabs.getTabs().size() - 1,
                    new CardinalTab(
                            null,
                            new CardinalView(showAsCurl(), showErrorDialog(), getCurrentConfig(), exportToCsv(), exportToBash(), sendRequest(), triggerUnsavedChangesMade()),
                            openNewFileIfNoneOpen(),
                            showConfirmDialog(),
                            save()
                    )
            );
            cardinalTabs.getSelectionModel().select(cardinalTabs.getTabs().size() - 2);
            return null;
        };
    }

    private Function<Void, Void> showAsCurl() {
        return (v) -> {
            CardinalTab currentTab = getCurrentTab();
            if (currentTab != null) {
                CardinalRequest request = ((CardinalView) currentTab.getContent()).getRequest();
                if (request.getUri().trim().length() == 0) {
                    showErrorDialog().apply("Please enter a URL.");
                } else {
                    ((CardinalView) currentTab.getContent()).loadCurlCommand(request.toCurl(currentConfig));
                }
            }
            return null;
        };
    }

    private Function<String, Void> showErrorDialog() {
        return (message) -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(message);
                alert.showAndWait();
            });
            return null;
        };
    }

    private BiFunction<File, String, Void> writeToFile() {
        return (file, data) -> {
            try {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(data);
                fileWriter.close();
            } catch (Exception e) {
                // TODO?
            }
            return null;
        };
    }

    private CardinalTab getCurrentTab() {
        return ((CardinalTab) cardinalTabs.getSelectionModel().getSelectedItem());
    }

    private Function<Void, Config> getCurrentConfig() {
        return (v) -> currentConfig;
    }

    private Function<List<CardinalRequestAndResponse>, Void> exportToCsv() {
        return (cardinalRequestAndResponses) -> {
            String header = CardinalRequest.csvHeaders + "," + CardinalResponse.csvHeaders();
            String content = cardinalRequestAndResponses.stream().map((reqAndRes) -> {
                return reqAndRes.getRequest().toCsv() + "," + (reqAndRes.getResponse() != null ? reqAndRes.getResponse().toCSV() : CardinalResponse.blank().toCSV());
            }).collect(Collectors.joining("\n"));

            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showSaveDialog(currentStage);
            if (file != null) {
                File fileWithExtension = null;
                if (!file.getAbsolutePath().endsWith(".csv")) {
                    fileWithExtension = new File(file.getAbsolutePath() + ".csv");
                } else {
                    fileWithExtension = file;
                }
                writeToFile().apply(fileWithExtension, header + "\n" + content);
            }
            return null;
        };
    }

    private BiFunction<List<CardinalRequest>, Integer, Void> exportToBash() {
        return (requests, throttle) -> {
            BashScript script = new BashScript(requests, currentConfig, throttle);
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showSaveDialog(currentStage);
            if (file != null) {
                try {
                    script.writeTo(file);
                } catch (IOException ioe) {
                    showErrorDialog().apply("Unable to write bash script to a file.");
                }
            }
            return null;
        };
    }

    private Function<CardinalRequest, CardinalResponse> sendRequest() {
        return (request) -> {
            long startTime = System.currentTimeMillis();
            CardinalHttpResponse httpResponse = new Http(request).send();
            long totalTime = System.currentTimeMillis() - startTime;
            return new CardinalResponse(httpResponse, totalTime);
        };
    }

    private Function<Void, Void> triggerUnsavedChangesMade() {
        return (v) -> {
            getCurrentTab().handleUnsavedChangesMade();
            return null;
        };
    }

    private Function<Void, Void> openNewFileIfNoneOpen() {
        return (v) -> {
            if (cardinalTabs.getTabs().size() == 0) {
                newTab().apply(null);
            }
            return null;
        };
    }

    private Function<Void, Void> save() {
        return (v) -> {
            CardinalTab currentTab = getCurrentTab();
            if (currentTab != null && currentTab.getCurrentFile() != null) {
                writeToFile().apply(
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
                if (result.get().getText().equals("Yes")) {
                    onYes.apply(null);
                } else if (result.get().getText().equals("No")) {
                    onNo.apply(null);
                } else if (result.get().getText().equals("Cancel")) {
                    onCancel.apply(null);
                }
            }
            return null;
        };
    }

    private Function<Void, Void> open() {
        return (v) -> {
          FileChooser fileChooser = new FileChooser();
          List<File> selectedFiles = fileChooser.showOpenMultipleDialog(currentStage);
          if (selectedFiles != null) {
              selectedFiles.forEach((file) -> {
                  try {
                      String lines = String.join("", Files.readAllLines(file.toPath()));
                      CardinalView cardinalView = new CardinalView(
                              showAsCurl(),
                              showErrorDialog(),
                              getCurrentConfig(),
                              exportToCsv(),
                              exportToBash(),
                              sendRequest(),
                              triggerUnsavedChangesMade()
                      );
                      addTab(new CardinalTab(file, cardinalView, openNewFileIfNoneOpen(), showConfirmDialog(), save()));
                      cardinalView.loadRequest(CardinalRequest.apply(lines));
                      getCurrentTab().setUnsavedChanges(false);
                  } catch (Exception e) {
                      e.printStackTrace();
                  }
              });
          }
          return null;
        };
    }

    private Function<Void, Void> clearAll() {
        return (v) -> {
          CardinalTab currentTab = getCurrentTab();
          if (currentTab != null) {
              ((CardinalView) currentTab.getContent()).clearAll();
          }
          return null;
        };
    }

    private Function<Void, Void> showBasicAuthInput() {
        return (v) -> {
            CardinalTab currentTab = getCurrentTab();
            if (currentTab != null) {
                BasicAuthInputDialog dialog = new BasicAuthInputDialog();
                Optional<BasicAuth> result = dialog.showAndWait();
                if (result.isPresent()) {
                    ((CardinalView) currentTab.getContent()).addHeader(result.get().asAuthHeader());
                }
            }
            return null;
        };
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
            if (result.isPresent()) {
                setEnvironmentVariables(result.get());
            }
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
                      writeToFile().apply(fileWithExtension, ((CardinalView) currentTab.getContent()).getRequest().toJson());
                      currentTab.setCurrentFile(fileWithExtension);
                      currentTab.setUnsavedChanges(false);
                  } else {
                      // Existing file so save and open in new tab
                      CardinalRequest request = ((CardinalView) currentTab.getContent()).getRequest();
                      writeToFile().apply(fileWithExtension, request.toJson());
                      CardinalView cardinalView = new CardinalView(showAsCurl(), showErrorDialog(), getCurrentConfig(), exportToCsv(), exportToBash(), sendRequest(), triggerUnsavedChangesMade());
                      addTab(new CardinalTab(fileWithExtension, cardinalView, openNewFileIfNoneOpen(), showConfirmDialog(), save()));
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
        writeToFile().apply(file, config.toJson());
    }

    private void addTab(Tab tab) {
        cardinalTabs.getTabs().add(cardinalTabs.getTabs().size() - 1, tab);
        cardinalTabs.getSelectionModel().select(cardinalTabs.getTabs().size() - 2);
    }

}
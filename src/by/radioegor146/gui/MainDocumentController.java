/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146.gui;

import by.radioegor146.FactorioLauncher;
import static by.radioegor146.FactorioLauncher.config;
import static by.radioegor146.FactorioLauncher.setDialogIcon;
import static by.radioegor146.RunHelper.getArchFolder;
import by.radioegor146.serverpinger.ServerPinger;
import by.radioegor146.serverpinger.classes.Client;
import by.radioegor146.serverpinger.classes.ListItem;
import by.radioegor146.serverpinger.classes.ModInfo;
import by.radioegor146.serverpinger.messages.ConnectionAcceptOrDenyMessage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 *
 * @author radioegor146
 */
public class MainDocumentController implements Initializable {

    public static MainDocumentController instance;

    @FXML
    private void closeWindow(ActionEvent event) {
        FactorioLauncher.config.lastServer = lastOkServer;
        FactorioLauncher.config.autoConnect = autoConnectCheckBox.isSelected();
        FactorioLauncher.config.noLogRotation = noLogRotationCheckBox.isSelected();
        try {
            FactorioLauncher.config.save();
        } catch (Exception e) {

        }
        ((Stage) mainPane.getScene().getWindow()).close();
    }

    @FXML
    private void minimizeWindow(ActionEvent event) {
        ((Stage) mainPane.getScene().getWindow()).setIconified(true);
    }

    private String lastOkServer = "";

    private ConnectionAcceptOrDenyMessage lastServerInfo = null;

    @FXML
    private void selectFactorioFolderHandler(ActionEvent event) {
        String prevPath = config.factorioPath.substring(0);
        while (true) {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Выберите папку с Factorio");
            File f = chooser.showDialog((Stage) mainPane.getScene().getWindow());
            if (f != null) {
                config.factorioPath = f.getAbsolutePath();
                try {
                    getArchFolder(false);
                    break;
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    setDialogIcon(alert);
                    alert.setTitle("Factorio Launcher");
                    alert.setHeaderText("Исполняемый файл Factorio не найден");
                    alert.setContentText("Скорее всего папка с Factorio выбрана некорректно. В папке должна быть папка bin. Выберите правильную папку");
                    alert.showAndWait();
                }
            }
            else {
                config.factorioPath = prevPath;
                return;
            }
        }
    }

    @FXML
    public void selectServerButtonHandler(ActionEvent event) {
        selectServerButton.setDisable(true);
        serverInfoLoadPane.setVisible(true);
        FactorioLauncher.modsHelper.updateCacheList();
        new Thread(() -> {
            errorText.setVisible(false);
            serverInfoVBox.setVisible(false);
            ServerPinger pinger = new ServerPinger();
            String serverIpString = serverIpField.getText();
            ConnectionAcceptOrDenyMessage serverInfo = null;
            if (serverIpString.length() > 0) {
                if (serverIpString.split(":").length == 1) {
                    serverInfo = pinger.getServerInfo(serverIpString, 34197);
                } else if (serverIpString.split(":").length == 2) {
                    try {
                        serverInfo = pinger.getServerInfo(serverIpString.split(":")[0], Integer.parseInt(serverIpString.split(":")[1]));
                    } catch (Exception e) {

                    }
                }
            }
            final ConnectionAcceptOrDenyMessage tServerInfo = serverInfo;
            lastServerInfo = serverInfo;
            StringBuilder sb = new StringBuilder();
            if (serverInfo != null) {
                if (serverInfo.online) {
                    serverNameText.setText(serverInfo.name);
                    serverDescriptionText.setText(serverInfo.serverDescription);
                    gameVersionText.setText(serverInfo.applicationVersion.toString());
                    playersCountText.setText((serverInfo.clients.length + (serverInfo.serverUsername.equals("<server>") ? 0 : 1)) + "/" + (serverInfo.maxPlayers == 0 ? "∞" : serverInfo.maxPlayers));
                    authNeededText.setText(serverInfo.requireUserVerification ? "Нет" : "Да");
                    passwordNeededText.setText(serverInfo.hasPassword ? "Да" : "Нет");
                    gameTimeText.setText(serverInfo.gameTimeElapsed / 60 / 24 + " дней " + (serverInfo.gameTimeElapsed / 60 % 24) + " часов " + (serverInfo.gameTimeElapsed % 60) + " минут");
                    Platform.runLater(() -> {
                        modsVBox.getChildren().clear();
                    });
                    for (ModInfo mod : serverInfo.mods) {
                        Platform.runLater(() -> {
                            Text text = new Text(mod.name + " " + mod.version);
                            text.setWrappingWidth(220);
                            text.setFont(Font.font("Lato", 14));
                            if (mod.name.equals("base")) {
                                text.setFill(Paint.valueOf("WHITE"));
                            } else {
                                if (FactorioLauncher.modsHelper.avaibleMods.contains(mod))
                                    text.setFill(Paint.valueOf("WHITE"));
                                else
                                    text.setFill(Paint.valueOf("#FF334C"));
                            }
                            modsVBox.getChildren().add(text);
                        });
                    }
                    Platform.runLater(() -> {
                        playersVBox.getChildren().clear();
                    });
                    if (!serverInfo.serverUsername.equals("<server>")) {
                        final String serverUsername = serverInfo.serverUsername;
                        Platform.runLater(() -> {
                            Text text = new Text(serverUsername);
                            text.setWrappingWidth(220);
                            text.setFont(Font.font("Lato", 14));
                            text.setFill(Paint.valueOf("LIME"));
                            playersVBox.getChildren().add(text);
                        });
                    }
                    for (Client client : serverInfo.clients) {
                        Platform.runLater(() -> {
                            Text text = new Text(client.username);
                            text.setWrappingWidth(220);
                            text.setFont(Font.font("Lato", 14));
                            if (tServerInfo.admins.contains(client.username)) {
                                text.setFill(Paint.valueOf("YELLOW"));
                            } else {
                                text.setFill(Paint.valueOf("WHITE"));
                            }
                            playersVBox.getChildren().add(text);
                        });
                    }
                    if (serverInfo.clients.length == 0 && serverInfo.serverUsername.equals("<server>")) {
                        Platform.runLater(() -> {
                            Text text = new Text("Никого нет");
                            text.setWrappingWidth(220);
                            text.setFont(Font.font("Lato", 14));
                            text.setFill(Paint.valueOf("WHITE"));
                            playersVBox.getChildren().add(text);
                        });
                    }
                    Platform.runLater(() -> {
                        adminsVBox.getChildren().clear();
                    });
                    for (String admin : serverInfo.admins) {
                        Platform.runLater(() -> {
                            Text text = new Text(admin);
                            text.setWrappingWidth(220);
                            text.setFont(Font.font("Lato", 14));
                            text.setFill(Paint.valueOf("WHITE"));
                            adminsVBox.getChildren().add(text);
                        });
                    }
                    if (serverInfo.admins.isEmpty()) {
                        Platform.runLater(() -> {
                            Text text = new Text("Нет");
                            text.setWrappingWidth(220);
                            text.setFont(Font.font("Lato", 14));
                            text.setFill(Paint.valueOf("WHITE"));
                            adminsVBox.getChildren().add(text);
                        });
                    }
                    Platform.runLater(() -> {
                        banlistVBox.getChildren().clear();
                    });
                    for (ListItem banItem : serverInfo.banlist) {
                        Platform.runLater(() -> {
                            Text text = new Text(banItem.username + (banItem.reason.isEmpty() ? "" : " - ") + banItem.reason);
                            text.setWrappingWidth(220);
                            text.setFont(Font.font("Lato", 14));
                            text.setFill(Paint.valueOf("WHITE"));
                            banlistVBox.getChildren().add(text);
                        });
                    }
                    if (serverInfo.banlist.length == 0) {
                        Platform.runLater(() -> {
                            Text text = new Text("Пусто");
                            text.setWrappingWidth(220);
                            text.setFont(Font.font("Lato", 14));
                            text.setFill(Paint.valueOf("WHITE"));
                            banlistVBox.getChildren().add(text);
                        });
                    }
                    Platform.runLater(() -> {
                        whitelistVBox.getChildren().clear();
                    });
                    for (ListItem whiteItem : serverInfo.whitelist) {
                        Platform.runLater(() -> {
                            Text text = new Text(whiteItem.username);
                            text.setWrappingWidth(220);
                            text.setFont(Font.font("Lato", 14));
                            text.setFill(Paint.valueOf("WHITE"));
                            whitelistVBox.getChildren().add(text);
                        });
                    }
                    if (serverInfo.whitelist.length == 0) {
                        Platform.runLater(() -> {
                            Text text = new Text("Выключен");
                            text.setWrappingWidth(220);
                            text.setFont(Font.font("Lato", 14));
                            text.setFill(Paint.valueOf("WHITE"));
                            whitelistVBox.getChildren().add(text);
                        });
                    }
                    lastOkServer = serverIpString;
                } else {
                    errorText.setText("Сервер оффлайн");
                    errorText.setVisible(true);
                }
            } else {
                errorText.setText("Некорректный формат IP адреса\n Корректный - <IP>[:Порт]");
                errorText.setVisible(true);
            }
            serverInfoVBox.setVisible(lastServerInfo != null && lastServerInfo.online);
            runGameButton.setDisable(lastServerInfo == null || !lastServerInfo.online);
            selectServerButton.setDisable(false);
            serverInfoLoadPane.setVisible(false);
        }).start();
    }

    @FXML
    private void runGameButton(ActionEvent event) {
        runGameButton.setDisable(true);
        new Thread(()->{
            try {
                final String lastOkServerPush = lastOkServer;
                FactorioLauncher.modsHelper.prepareAndRun(lastServerInfo.mods, noLogRotationCheckBox.isSelected(), (autoConnectCheckBox.isSelected() ? lastOkServerPush : null));
            }
            catch (Exception e) {
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    setDialogIcon(alert);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Произошла ошибка при запуске Factorio");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                    runGameButton.setDisable(false);
                });
            }
        }).start();
    }
    
    private double initialX = 0;
    private double initialY = 0;

    @FXML
    private void dragWindow(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            this.initialX = event.getSceneX();
            this.initialY = event.getSceneY();
            event.consume();
        }
        if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            mainPane.getScene().getWindow().setX(event.getScreenX() - this.initialX);
            mainPane.getScene().getWindow().setY(event.getScreenY() - this.initialY);
            event.consume();
        }
    }

    @FXML
    private AnchorPane mainPane;

    @FXML
    private TextField serverIpField;

    @FXML
    private Button runGameButton;

    @FXML
    private Button selectServerButton;

    @FXML
    private Text errorText;

    @FXML
    private Node serverInfoVBox;

    @FXML
    private Text serverNameText;
    @FXML
    private Text serverDescriptionText;
    @FXML
    private Text gameVersionText;
    @FXML
    private Text playersCountText;
    @FXML
    private Text authNeededText;
    @FXML
    private Text passwordNeededText;
    @FXML
    private Text gameTimeText;
    @FXML
    private VBox modsVBox;
    @FXML
    private VBox playersVBox;
    @FXML
    private VBox adminsVBox;
    @FXML
    private VBox banlistVBox;
    @FXML
    private VBox whitelistVBox;

    @FXML
    public Pane serverInfoLoadPane;

    @FXML
    private Text factorioFolderText;

    @FXML
    private CheckBox noLogRotationCheckBox;
    @FXML
    private CheckBox autoConnectCheckBox;
    
    @FXML
    private Pane loadPane;
    @FXML
    private ProgressBar loadProgressBar;
    @FXML
    private Text loadInfoText;
    @FXML
    private Text percentLoadText;
    
    public void showInfo(StateInfo stateInfo) {
        if (stateInfo.ready) {
            closeWindow(null);
            return;
        }
        loadPane.setVisible(true);
        loadInfoText.setText(stateInfo.infoText);
        percentLoadText.setText(Math.round(stateInfo.progress * 100) + "%");
        loadProgressBar.setProgress(stateInfo.progress);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorText.managedProperty().bind(errorText.visibleProperty());
        serverInfoVBox.managedProperty().bind(serverInfoVBox.visibleProperty());
        errorText.setVisible(false);
        serverInfoVBox.setVisible(false);
        factorioFolderText.setText("Папка с Factorio: " + FactorioLauncher.config.factorioPath);
        lastOkServer = FactorioLauncher.config.lastServer;
        serverIpField.setText(FactorioLauncher.config.lastServer);
        noLogRotationCheckBox.setSelected(FactorioLauncher.config.noLogRotation);
        autoConnectCheckBox.setSelected(FactorioLauncher.config.autoConnect);
        instance = this;
    }

    public void hideProgess() {
        loadPane.setVisible(false);
    }
    
    public static class StateInfo {
        public String infoText;
        public double progress;
        public boolean ready;
        
        public StateInfo(String infoText, double progress, boolean ready) {
            this.infoText = infoText;
            this.progress = progress;
            this.ready = ready;
        }
    }
}

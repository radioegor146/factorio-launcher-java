/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146;

import static by.radioegor146.RunHelper.getArchFolder;
import by.radioegor146.gui.MainDocumentController;
import java.io.File;
import java.util.Random;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author radioegor146
 */
public class FactorioLauncher extends Application {

    public static Random random = new Random();
    public static Config config;
    public static ModsHelper modsHelper = new ModsHelper();

    public static void setDialogIcon(Alert alert) {
        ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(FactorioLauncher.class.getResource("/fxml/images/icon.png").toString()));
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        stage.getIcons().add(new Image(FactorioLauncher.class.getResource("/fxml/images/icon.png").toString()));
        config = new Config();
        try {
            config.load();
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.INFORMATION);
            setDialogIcon(alert);
            alert.setTitle("Factorio Launcher");
            alert.setHeaderText("Первый запуск");
            alert.setContentText("Похоже что вы запускаете лаунчер в первый раз. Пожалуйста, выберите папку Factorio.");
            alert.showAndWait();
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Выберите папку с Factorio");
            File f = chooser.showDialog(stage);
            if (f != null) {
                config.factorioPath = f.getAbsolutePath();
            } else {
                return;
            }
        }
        try {
            config.save();
        } catch (Exception e) {

        }
        while (true) {
            try {
                getArchFolder(false);
                break;
            } catch (Exception e) {
                Alert alert = new Alert(AlertType.ERROR);
                setDialogIcon(alert);
                alert.setTitle("Factorio Launcher");
                alert.setHeaderText("Исполняемый файл Factorio не найден");
                alert.setContentText("Скорее всего папка с Factorio выбрана некорректно. В папке должна быть папка bin. Выберите правильную папку");
                alert.showAndWait();
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Выберите папку с Factorio");
                File f = chooser.showDialog(stage);
                if (f != null) {
                    config.factorioPath = f.getAbsolutePath();
                } else {
                    return;
                }
            }
        }
        modsHelper.prepare();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Factorio Launcher Alpha v0.1");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainDocument.fxml"));
        MainDocumentController controller = new MainDocumentController();
        loader.setController(controller);
        Scene scene = new Scene(loader.load());
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
        controller.serverInfoLoadPane.setVisible(false); // KOSTYL (fuck JAVA)
        if (config.lastServer != null && !"".equals(config.lastServer)) {
            controller.selectServerButtonHandler(null);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false");
        launch(args);
    }

}

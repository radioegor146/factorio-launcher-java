package by.radioegor146;

import by.radioegor146.gui.MainDocumentController;
import by.radioegor146.helpers.GuiHelper;
import by.radioegor146.helpers.ModsHelper;
import by.radioegor146.helpers.RunHelper;
import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
    
    public static FactorioLauncherConfig config;

    @Override
    public void start(Stage stage) throws Exception {
        stage.getIcons().add(new Image(FactorioLauncher.class.getResource("/fxml/images/icon.png").toString()));
        config = new FactorioLauncherConfig();
        try {
            config.load();
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Factorio Launcher");
            alert.setHeaderText("Первый запуск");
            alert.setContentText("Похоже что вы запускаете лаунчер в первый раз. Пожалуйста, выберите папку Factorio.");
            GuiHelper.prepareDialog(alert);
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
        while (true) {
            try {
                RunHelper.getArchFolder(false);
                break;
            } catch (Exception e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Factorio Launcher");
                alert.setHeaderText("Исполняемый файл Factorio не найден");
                alert.setContentText("Скорее всего папка с Factorio выбрана некорректно. В папке должна быть папка bin. Выберите правильную папку");
                GuiHelper.prepareDialog(alert);
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
        ModsHelper.prepare();
        try {
            config.save();
        } catch (IOException e) {
        }
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Factorio Launcher Alpha v0.1");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainDocument.fxml"));
        MainDocumentController controller = new MainDocumentController();
        loader.setController(controller);
        Object sceneObject = loader.load();
        Scene scene = new Scene((Parent)sceneObject);
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

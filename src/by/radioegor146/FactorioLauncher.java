package by.radioegor146;

import by.radioegor146.gui.MainDocumentController;
import by.radioegor146.helpers.GuiHelper;
import by.radioegor146.helpers.ModsHelper;
import by.radioegor146.helpers.RunHelper;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
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
    
    public static ResourceBundle currentBundle;

    @Override
    public void start(Stage stage) throws Exception {
        currentBundle = ResourceBundle.getBundle("bundles.MainBundle");
        stage.getIcons().add(new Image(FactorioLauncher.class.getResource("/fxml/images/icon.png").toString()));
        config = new FactorioLauncherConfig();
        try {
            config.load();
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Factorio Launcher");
            alert.setHeaderText(currentBundle.getString("first-run"));
            alert.setContentText(currentBundle.getString("first-run-text"));
            GuiHelper.prepareDialog(alert);
            alert.showAndWait();
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(currentBundle.getString("select-factorio-folder"));
            File f = chooser.showDialog(stage);
            if (f != null) {
                config.factorioPath = f.getAbsolutePath();
            } else {
                return;
            }
        }
        while (true) {
            try {
                RunHelper.getArchBinFile(false);
                break;
            } catch (Exception e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Factorio Launcher");
                alert.setHeaderText(currentBundle.getString("bin-file-is-not-found"));
                alert.setContentText(currentBundle.getString("incorrect-factorio-folder"));
                GuiHelper.prepareDialog(alert);
                alert.showAndWait();
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle(currentBundle.getString("select-factorio-folder"));
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
        stage.setTitle("Factorio Launcher");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainDocument.fxml"));
        loader.setResources(currentBundle);
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

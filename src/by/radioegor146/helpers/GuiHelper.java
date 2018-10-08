/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146.helpers;

import by.radioegor146.FactorioLauncher;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 *
 * @author radioegor146
 */
public class GuiHelper {

    public static void prepareDialog(Alert alert) {
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(FactorioLauncher.class.getResource("/fxml/images/icon.png").toString()));
    }
}

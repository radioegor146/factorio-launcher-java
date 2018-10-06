/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146;

import java.nio.file.Paths;
import java.io.File;
import java.nio.file.Files;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 *
 * @author radioegor146
 */
public class RunHelper {
    public static void runFactorio() throws Exception {
        ProcessBuilder pBuilder = new ProcessBuilder();
        pBuilder.directory(Paths.get(FactorioLauncher.config.factorioPath).toFile()).command(getArchFolder(), "--mod-directory", Paths.get(FactorioLauncher.config.factorioPath, "launcher", "current").toFile().getAbsolutePath()).start();
    }
    
    public static String getArchFolder() throws Exception {
        boolean is64bit;
        if (System.getProperty("os.name").contains("Windows")) {
            is64bit = (System.getenv("ProgramFiles(x86)") != null);
        } else {
            is64bit = (System.getProperty("os.arch").contains("64"));
        }
        String currentArch = is64bit ? "x64" : "x32";
        final String rightArch = is64bit ? "x64" : "x32";
        if (Files.exists(Paths.get(FactorioLauncher.config.factorioPath, "bin", currentArch, "factorio")) || Files.exists(Paths.get(FactorioLauncher.config.factorioPath, "bin", currentArch, "factorio.exe"))) {
            return FactorioLauncher.config.factorioPath + File.separator + "bin" + File.separator + currentArch + File.separator + (System.getProperty("os.name").contains("Windows") ? "factorio.exe" : "factorio") ;
        } else {
            currentArch = is64bit ? "x32" : "x64";
            final String nowArch = currentArch;
            if (Files.exists(Paths.get(FactorioLauncher.config.factorioPath, "bin", currentArch, "factorio")) || Files.exists(Paths.get(FactorioLauncher.config.factorioPath, "bin", currentArch, "factorio.exe"))) {
                Platform.runLater(()->{
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Неправильный запуск");
                    alert.setHeaderText("Factorio своей архитектуры (" + rightArch + ") не найдена!");
                    alert.setContentText("Скорее всего либо Factorio, либо Java была установлена не для нужной архитектуры (" + rightArch + "). Была запущена Factorio другой архитектуры (" + nowArch + ").");
                    alert.show();
                });
                return FactorioLauncher.config.factorioPath + File.separator + "bin" + File.separator + currentArch + File.separator + (System.getProperty("os.name").contains("Windows") ? "factorio.exe" : "factorio") ;
            }
        }
        throw new Exception("Исполняемый файл Factorio не найден (" + (System.getProperty("os.name").contains("Windows") ? "factorio.exe" : "factorio") + ")");
    }
}

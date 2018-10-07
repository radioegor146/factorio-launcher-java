/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146;

import static by.radioegor146.FactorioLauncher.setDialogIcon;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 *
 * @author radioegor146
 */
public class RunHelper {

    public static void runFactorio(Path modDir, boolean noLogRotation, String serverIp) throws Exception {
        ProcessBuilder pBuilder = new ProcessBuilder();
        pBuilder.directory(Paths.get(FactorioLauncher.config.factorioPath).toFile()).command(getArchFolder(true), "--mod-directory", modDir.toFile().getAbsolutePath(), noLogRotation ? "--no-log-rotation" : "", serverIp == null ? "" : "--mp-connect", serverIp != null ? serverIp : "").start();
    }

    public static void waitForRunLater() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(() -> semaphore.release());
        semaphore.acquire();
    }

    public static String getArchFolder(boolean shouldshow) throws Exception {
        boolean is64bit;
        if (System.getProperty("os.name").contains("Windows")) {
            is64bit = (System.getenv("ProgramFiles(x86)") != null);
        } else {
            is64bit = (System.getProperty("os.arch").contains("64"));
        }
        String currentArch = is64bit ? "x64" : "x32";
        final String rightArch = is64bit ? "x64" : "x32";
        if (Files.exists(Paths.get(FactorioLauncher.config.factorioPath, "bin", currentArch, "factorio")) || Files.exists(Paths.get(FactorioLauncher.config.factorioPath, "bin", currentArch, "factorio.exe"))) {
            return FactorioLauncher.config.factorioPath + File.separator + "bin" + File.separator + currentArch + File.separator + (System.getProperty("os.name").contains("Windows") ? "factorio.exe" : "factorio");
        } else {
            currentArch = is64bit ? "x32" : "x64";
            final String nowArch = currentArch;
            if (Files.exists(Paths.get(FactorioLauncher.config.factorioPath, "bin", currentArch, "factorio")) || Files.exists(Paths.get(FactorioLauncher.config.factorioPath, "bin", currentArch, "factorio.exe"))) {
                if (shouldshow) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(AlertType.WARNING);
                        setDialogIcon(alert);
                        alert.setTitle("Неправильный запуск");
                        alert.setHeaderText("Factorio своей архитектуры (" + rightArch + ") не найдена!");
                        alert.setContentText("Скорее всего либо Factorio, либо Java была установлена не для нужной архитектуры (" + rightArch + "). Была запущена Factorio другой архитектуры (" + nowArch + ").");
                        alert.showAndWait();
                    });
                    waitForRunLater();
                }
                return FactorioLauncher.config.factorioPath + File.separator + "bin" + File.separator + currentArch + File.separator + (System.getProperty("os.name").contains("Windows") ? "factorio.exe" : "factorio");
            }
        }
        throw new Exception("Исполняемый файл Factorio не найден (" + (System.getProperty("os.name").contains("Windows") ? "factorio.exe" : "factorio") + ")");
    }
}

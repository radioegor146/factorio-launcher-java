/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146.helpers;

import by.radioegor146.FactorioLauncher;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 *
 * @author radioegor146
 */
public class RunHelper {

    public static void runFactorio(Path modDir, boolean noLogRotation, String serverIp, String additionalArgs) throws Exception {
        ProcessBuilder pBuilder = new ProcessBuilder();
        ArrayList<String> args = new ArrayList<>();
        args.add(getArchBinFile(true));
        args.add("--mod-directory");
        args.add(modDir.toFile().getAbsolutePath());
        if (noLogRotation)
            args.add("--no-log-rotation");
        if (serverIp != null) {
            args.add("--mp-connect");
            args.add(serverIp);
        }
        args.addAll(Arrays.asList(additionalArgs.split(" ")).stream().filter(str -> !str.isEmpty()).collect(Collectors.toList()));
        pBuilder = pBuilder.directory(Paths.get(FactorioLauncher.config.factorioPath).toFile()).command(args);
        pBuilder.inheritIO();
        pBuilder.start();
    }

    public static void waitForRunLater() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(() -> semaphore.release());
        semaphore.acquire();
    }

    public static String getArchBinFile(boolean shouldShowArchError) throws Exception {
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
                if (shouldShowArchError) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(AlertType.WARNING);
                        alert.setTitle("Неправильный запуск");
                        alert.setHeaderText("Factorio своей архитектуры (" + rightArch + ") не найдена!");
                        alert.setContentText("Скорее всего либо Factorio, либо Java была установлена не для нужной архитектуры (" + rightArch + "). Была запущена Factorio другой архитектуры (" + nowArch + ").");
                        GuiHelper.prepareDialog(alert);
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

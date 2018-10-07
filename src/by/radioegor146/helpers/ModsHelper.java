package by.radioegor146.helpers;

import by.radioegor146.FactorioLauncher;
import by.radioegor146.gui.MainDocumentController;
import by.radioegor146.gui.MainDocumentController.StateInfo;
import by.radioegor146.serverpinger.classes.ModInfo;
import by.radioegor146.serverpinger.classes.Version;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.Semaphore;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;

/**
 *
 * @author radioegor146
 */
public class ModsHelper {

    private static void createModList(Path where, ModInfo[] mods) throws IOException {
        JsonObject object = new JsonObject();
        JsonArray modsArray = new JsonArray();
        for (ModInfo mod : mods) {
            JsonObject modObject = new JsonObject();
            modObject.add("name", mod.name);
            modObject.add("enabled", true);
            modsArray.add(modObject);
        }
        object.add("mods", modsArray);
        Files.write(where.resolve("mod-list.json"), object.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static Path getModCacheDir() {
        return Paths.get(FactorioLauncher.config.modCachePath);
    }

    private static Path getAndCreateTempDir() throws IOException {
        Path tempPath = Paths.get(FactorioLauncher.config.tempPath, "flauncher" + System.nanoTime());
        Files.createDirectories(tempPath);
        return tempPath;
    }

    public static void prepare() throws IOException {
        if (Files.notExists(getModCacheDir())) {
            Files.createDirectories(getModCacheDir());
        }
    }

    public static void waitForRunLater() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(() -> semaphore.release());
        semaphore.acquire();
    }

    public static boolean prepareAndRun(ModInfo[] mods, boolean noLogRotation, String serverIp) throws Exception {
        Platform.runLater(() -> {
            MainDocumentController.instance.showInfo(new StateInfo("Загрузка modcache...", 0, false));
        });
        updateCacheList();
        Platform.runLater(() -> {
            MainDocumentController.instance.showInfo(new StateInfo("Загрузка модов...", 0.10, false));
        });
        int index = 0;
        for (ModInfo mod : mods) {
            if (mod.name.equals("base")) {
                continue;
            }
            if (!avaibleMods.contains(mod)) {
                final int outIndex = index;
                Platform.runLater(() -> {
                    MainDocumentController.instance.showInfo(new StateInfo("Загрузка " + mod.toString() + "...", 0.10 + outIndex * (0.7 / (mods.length - 1)), false));
                });
                downloadMod(mod);
                index++;
            }
        }
        Platform.runLater(() -> {
            MainDocumentController.instance.showInfo(new StateInfo("Подготовка к запуску", 0.90, false));
        });
        Path modsDir = getAndCreateTempDir();
        createModList(modsDir, mods);
        boolean okWithNoMods = false;
        for (ModInfo mod : mods) {
            if (mod.name.equals("base")) {
                continue;
            }
            if (Files.notExists(getModCacheDir().resolve(mod.name + "_" + mod.version + ".zip")) && !okWithNoMods) {
                final ButtonType yesButton = new ButtonType("Да", ButtonData.YES);
                final SimpleObjectProperty returnButton = new SimpleObjectProperty();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    GuiHelper.setDialogIcon(alert);
                    alert.setTitle("Мода не существует");
                    alert.setHeaderText("Мода '" + mod.name + " " + mod.version + "' не существует в modcache!");
                    alert.setContentText("Скорее всего либо данный мод отсутвсует на официальном модпортале, либо на пиратском модпортале. Продолжить запуск Factorio?");
                    alert.getButtonTypes().setAll(yesButton, new ButtonType("Нет", ButtonData.NO));
                    returnButton.set(alert.showAndWait().get());
                });
                waitForRunLater();
                if (returnButton.get() == yesButton) {
                    okWithNoMods = true;
                } else {
                    Files.walk(modsDir)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                    Platform.runLater(() -> {
                        MainDocumentController.instance.hideProgess();
                    });
                    return false;
                }
            }
            if (Files.exists(getModCacheDir().resolve(mod.name + "_" + mod.version + ".zip"))) {
                Files.createSymbolicLink(modsDir.resolve(mod.name + "_" + mod.version + ".zip"), getModCacheDir().resolve(mod.name + "_" + mod.version + ".zip"));
            }
        }
        Platform.runLater(() -> {
            MainDocumentController.instance.showInfo(new StateInfo("Запуск", 1, false));
        });
        RunHelper.runFactorio(modsDir, noLogRotation, serverIp);
        Platform.runLater(() -> {
            MainDocumentController.instance.showInfo(new StateInfo("Запуск", 1, true));
        });
        return true;
    }

    public static void updateCacheList() {
        for (File modFile : getModCacheDir().toFile().listFiles()) {
            String fileName = modFile.getName();
            if (!fileName.endsWith(".zip")) {
                continue;
            }
            fileName = fileName.substring(0, fileName.length() - 4);
            String[] tData = fileName.split("_");
            if (tData.length < 2) {
                continue;
            }
            String modVersionString = tData[tData.length - 1];
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tData.length - 1; i++) {
                sb = sb.append(tData[i]).append((i == tData.length - 2) ? "" : "_");
            }
            String modName = sb.toString();
            Version modVersion = new Version();
            tData = modVersionString.split("\\.");
            try {
                modVersion.majorVersion = Short.parseShort(tData[0]);
                modVersion.minorVersion = Short.parseShort(tData[1]);
                modVersion.subVersion = Short.parseShort(tData[2]);
            } catch (NumberFormatException e) {
                continue;
            }
            avaibleMods.add(new ModInfo(modName, modVersion, -1));
        }
    }

    private static void downloadMod(ModInfo mod) throws Exception {
        URL website = new URL("http://185.63.188.9/mods/" + URLEncoder.encode(mod.name, "UTF-8").replace("+", "%20") + "/" + mod.version + ".zip");
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream(getModCacheDir().resolve(mod.name + "_" + mod.version + ".zip").toFile());
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }

    public static final HashSet<ModInfo> avaibleMods = new HashSet<>();

}

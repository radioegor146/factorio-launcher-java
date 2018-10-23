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
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.Semaphore;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import java.util.ArrayList;
import java.util.Arrays;

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
        return Paths.get(FactorioLauncher.config.launcherPath, "modcache");
    }

    private static Path getAndCreateTempDir() throws IOException {
        Path tempPath = Paths.get(FactorioLauncher.config.tempPath, "flauncher" + System.nanoTime());
        Files.createDirectories(tempPath);
        return tempPath;
    }
    
    private static Path getServerDir() throws IOException {
        Path serverPath = Paths.get(FactorioLauncher.config.launcherPath, "servers", MainDocumentController.instance.lastOkServer.replaceAll("[^a-zA-Z0-9\\.\\-]", "_"));
        Files.createDirectories(serverPath);
        return serverPath;
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

    public static boolean prepareAndRun(ModInfo[] mods, boolean noLogRotation, String serverIp, String additionalArgs) throws Exception {
        Platform.runLater(() -> {
            MainDocumentController.instance.showInfo(new StateInfo(FactorioLauncher.currentBundle.getString("loading-modcache"), 0, false));
        });
        updateCacheList();
        Platform.runLater(() -> {
            MainDocumentController.instance.showInfo(new StateInfo(FactorioLauncher.currentBundle.getString("loading-mods"), 0.10, false));
        });
        int index = 0;
        for (ModInfo mod : mods) {
            if (mod.name.equals("base")) {
                continue;
            }
            if (!avaibleMods.contains(mod)) {
                final int outIndex = index;
                Platform.runLater(() -> {
                    MainDocumentController.instance.showInfo(new StateInfo(String.format(FactorioLauncher.currentBundle.getString("loading-mod"), mod), 0.10 + outIndex * (0.7 / (mods.length - 1)), false));
                });
                downloadMod(mod);
                index++;
            }
        }
        Path modsDir = null;
        if (FactorioLauncher.config.useSymlinks) {
            Platform.runLater(() -> {
                MainDocumentController.instance.showInfo(new StateInfo(FactorioLauncher.currentBundle.getString("preparing-mods-creating-links"), .9, false));
            });
            modsDir = getAndCreateTempDir();
            createModList(modsDir, mods);
            boolean okWithNoMods = false;
            for (ModInfo mod : mods) {
                if (mod.name.equals("base")) {
                    continue;
                }
                if ((Files.notExists(getModCacheDir().resolve(mod.name + "_" + mod.version + ".zip")) && Files.notExists(getModCacheDir().resolve(mod.name + "_" + mod.version)) && !Files.isDirectory(getModCacheDir().resolve(mod.name + "_" + mod.version))) && !okWithNoMods) {
                    final ButtonType yesButton = new ButtonType(FactorioLauncher.currentBundle.getString("yes"), ButtonData.YES);
                    final SimpleObjectProperty returnButton = new SimpleObjectProperty();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle(FactorioLauncher.currentBundle.getString("mod-does-not-exists"));
                        alert.setHeaderText(String.format(FactorioLauncher.currentBundle.getString("mod-_-does-not-exists-in-modcache"), mod));
                        alert.setContentText(FactorioLauncher.currentBundle.getString("probably-mod-does-not-exists-in-official-mod-portal-run-factorio"));
                        alert.getButtonTypes().setAll(yesButton, new ButtonType(FactorioLauncher.currentBundle.getString("no"), ButtonData.NO));
                        GuiHelper.prepareDialog(alert);
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
                if (Files.exists(getModCacheDir().resolve(mod.name + "_" + mod.version))) {
                    Files.createSymbolicLink(modsDir.resolve(mod.name + "_" + mod.version), getModCacheDir().resolve(mod.name + "_" + mod.version));
                } else if (Files.exists(getModCacheDir().resolve(mod.name + "_" + mod.version + ".zip"))) {
                    Files.createSymbolicLink(modsDir.resolve(mod.name + "_" + mod.version + ".zip"), getModCacheDir().resolve(mod.name + "_" + mod.version + ".zip"));
                }

            }
        } else {
            Platform.runLater(() -> {
                MainDocumentController.instance.showInfo(new StateInfo(FactorioLauncher.currentBundle.getString("preparing-mods-removing-ex-files"), .9, false));
            });
            modsDir = getServerDir();
            HashSet<ModInfo> tMods = getModsFromDir(modsDir);
            HashSet<ModInfo> neededMods = new HashSet<>(Arrays.asList(mods));
            tMods.removeAll(neededMods);
            for (ModInfo mod : tMods) {
                if (Files.exists(modsDir.resolve(mod.name + "_" + mod.version)) && Files.isDirectory(modsDir.resolve(mod.name + "_" + mod.version)))
                    removeDirectory(modsDir.resolve(mod.name + "_" + mod.version));
                if (Files.exists(modsDir.resolve(mod.name + "_" + mod.version + ".zip")))
                    Files.delete(modsDir.resolve(mod.name + "_" + mod.version + ".zip"));
            }
            Platform.runLater(() -> {
                MainDocumentController.instance.showInfo(new StateInfo(FactorioLauncher.currentBundle.getString("preparing-mods-copying-files"), .9, false));
            });
            createModList(modsDir, mods);
            boolean okWithNoMods = false;
            for (ModInfo mod : mods) {
                if (mod.name.equals("base")) {
                    continue;
                }
                if ((Files.notExists(getModCacheDir().resolve(mod.name + "_" + mod.version + ".zip")) && Files.notExists(getModCacheDir().resolve(mod.name + "_" + mod.version)) && !Files.isDirectory(getModCacheDir().resolve(mod.name + "_" + mod.version))) && !okWithNoMods) {
                    final ButtonType yesButton = new ButtonType(FactorioLauncher.currentBundle.getString("yes"), ButtonData.YES);
                    final SimpleObjectProperty returnButton = new SimpleObjectProperty();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle(FactorioLauncher.currentBundle.getString("mod-does-not-exists"));
                        alert.setHeaderText(String.format(FactorioLauncher.currentBundle.getString("mod-_-does-not-exists-in-modcache"), mod.name, mod.version));
                        alert.setContentText(FactorioLauncher.currentBundle.getString("probably-mod-does-not-exists-in-official-mod-portal-run-factorio"));
                        alert.getButtonTypes().setAll(yesButton, new ButtonType(FactorioLauncher.currentBundle.getString("no"), ButtonData.NO));
                        GuiHelper.prepareDialog(alert);
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
                if (Files.exists(getModCacheDir().resolve(mod.name + "_" + mod.version)) && !Files.exists(modsDir.resolve(mod.name + "_" + mod.version))) {
                    Files.walkFileTree(getModCacheDir().resolve(mod.name + "_" + mod.version), new CopyFileVisitior(getModCacheDir().resolve(mod.name + "_" + mod.version), modsDir.resolve(mod.name + "_" + mod.version)));
                } else if (Files.exists(getModCacheDir().resolve(mod.name + "_" + mod.version + ".zip")) && !Files.exists(modsDir.resolve(mod.name + "_" + mod.version + ".zip"))) {
                    Files.copy(getModCacheDir().resolve(mod.name + "_" + mod.version + ".zip"), modsDir.resolve(mod.name + "_" + mod.version + ".zip"), REPLACE_EXISTING);
                }
            }
        }
        Platform.runLater(() -> {
            MainDocumentController.instance.showInfo(new StateInfo(FactorioLauncher.currentBundle.getString("running-game"), 1, false));
        });
        RunHelper.runFactorio(modsDir, noLogRotation, serverIp, additionalArgs);
        Platform.runLater(() -> {
            MainDocumentController.instance.showInfo(new StateInfo(FactorioLauncher.currentBundle.getString("running-game"), 1, true));
        });
        return true;
    }
    
    private static void removeDirectory(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    static class CopyFileVisitior extends SimpleFileVisitor<Path> {

        final Path source;
        final Path target;

        public CopyFileVisitior(Path source, Path target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {

            Path newDirectory = target.resolve(source.relativize(dir));
            try {
                Files.copy(dir, newDirectory, REPLACE_EXISTING);
            } catch (FileAlreadyExistsException ioException) {
                //log it and move
                return SKIP_SUBTREE; // skip processing
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

            Path newFile = target.resolve(source.relativize(file));

            try {
                Files.copy(file, newFile, REPLACE_EXISTING);
            } catch (IOException ioException) {
                //log it and move
            }

            return FileVisitResult.CONTINUE;

        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (exc instanceof FileSystemLoopException) {
                //log error
            } else {
                //log error
            }
            return CONTINUE;
        }
    }

    public static HashSet<ModInfo> getModsFromDir(Path dir) {
        HashSet<ModInfo> mods = new HashSet<>();
        for (File modFile : dir.toFile().listFiles()) {
            String fileName = modFile.getName();
            if (!fileName.endsWith(".zip") && !modFile.isDirectory()) {
                continue;
            }
            if (fileName.endsWith(".zip")) {
                fileName = fileName.substring(0, fileName.length() - 4);
            }
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
            mods.add(new ModInfo(modName, modVersion, modFile.isDirectory() ? -2 : -1));
        }
        return mods;
    }
    
    public static void updateCacheList() {
        avaibleMods.clear();
        avaibleMods.addAll(getModsFromDir(getModCacheDir()));
    }

    private static void downloadMod(ModInfo mod) throws Exception {
        URL website = new URL("https://factorio-launcher-mods.storage.googleapis.com/" + URLEncoder.encode(mod.name, "UTF-8").replace("+", "%20") + "/" + mod.version + ".zip");
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream(getModCacheDir().resolve(mod.name + "_" + mod.version + ".zip").toFile());
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }

    public static final HashSet<ModInfo> avaibleMods = new HashSet<>();

}

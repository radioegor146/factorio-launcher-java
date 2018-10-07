/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146;


import by.radioegor146.serverpinger.classes.ModInfo;
import by.radioegor146.serverpinger.classes.Version;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.LinkOption;
import java.io.IOException;
import java.util.ArrayList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import java.io.File;
import java.util.Comparator;
/**
 *
 * @author radioegor146
 */
public class ModsHelper {
    private void createModList(Path where, ModInfo[] mods) throws IOException {
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
    
    public void prepare() throws IOException {
        if (Files.notExists(Paths.get(FactorioLauncher.config.factorioPath, "launcher")))
            Files.createDirectories(Paths.get(FactorioLauncher.config.factorioPath, "launcher"));
        if (Files.notExists(Paths.get(FactorioLauncher.config.factorioPath, "launcher", "modcache")))
            Files.createDirectories(Paths.get(FactorioLauncher.config.factorioPath, "launcher", "modcache"));
    }
    
    private String getZipFileVersion(Version v) {
        return v.majorVersion + "_" + v.minorVersion + "_" + v.subVersion;
    }
    
    public boolean prepareAndRun(ModInfo[] mods, boolean noLogRotation) throws Exception {
        Path modsDir = Files.createTempDirectory("flaunchermods");
        createModList(modsDir, mods);
        boolean okWithNoMods = false;
        for (ModInfo mod : mods) {
            if (mod.name.equals("base"))
                continue;
            if (Files.notExists(Paths.get(FactorioLauncher.config.factorioPath, "launcher", "modcache", mod.name + "-" + getZipFileVersion(mod.version) + ".zip")) && !okWithNoMods)
            {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Мода не существует");
                alert.setHeaderText("Мода '" + mod.name + " " + mod.version + "' не существует в modcache!");
                alert.setContentText("Скорее всего либо данный мод отсутвсует на официальном модпортале, либо на пиратском модпортале. Продолжить запуск Factorio?");
                ButtonType yesButton = new ButtonType("Да", ButtonData.YES);
                alert.getButtonTypes().setAll(yesButton, new ButtonType("Нет", ButtonData.NO));
                if (alert.showAndWait().get() == yesButton)
                    okWithNoMods = true;
                else {
                    Files.walk(modsDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
                    return false;
                }
            }
            if (Files.exists(Paths.get(FactorioLauncher.config.factorioPath, "launcher", "modcache", mod.name + "-" + getZipFileVersion(mod.version) + ".zip")))
                Files.createLink(modsDir.resolve(mod.name + "-" + getZipFileVersion(mod.version) + ".zip"), Paths.get(FactorioLauncher.config.factorioPath, "launcher", "modcache", mod.name + "-" + getZipFileVersion(mod.version) + ".zip"));
        }
        RunHelper.runFactorio(modsDir, noLogRotation);
        return true;
    }
    
    private ArrayList<ModInfo> avaibleMods = new ArrayList<>();
    
}

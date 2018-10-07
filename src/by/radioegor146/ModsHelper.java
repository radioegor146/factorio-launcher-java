/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146;


import by.radioegor146.serverpinger.classes.ModInfo;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.LinkOption;
import java.io.IOException;
import java.util.ArrayList;
/**
 *
 * @author radioegor146
 */
public class ModsHelper {
    public void createModList(Path where, ModInfo[] mods) throws IOException {
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
    
    public void prepareAndRun(ModInfo[] mods, boolean noLogRotation) throws Exception {
        Path modsDir = Files.createTempDirectory("flaunchermods");
        createModList(modsDir, mods);
        for (ModInfo mod : mods) {
            if (mod.name.equals("base"))
                continue;
            Files.createLink(modsDir.resolve(mod.name + "-" + mod.version + ".zip"), Paths.get(FactorioLauncher.config.factorioPath, "launcher", "modcache", mod.name + "-" + mod.version + ".zip"));
        }
        RunHelper.runFactorio(modsDir, noLogRotation);
    }
    
    private ArrayList<ModInfo> avaibleMods = new ArrayList<>();
    
}

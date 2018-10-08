/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146;

import by.radioegor146.helpers.OSHelper;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author radioegor146
 */
public final class FactorioLauncherConfig {

    public String factorioPath = "Не задано";
    public String modCachePath = OSHelper.getDefaultModCacheDir().toFile().getAbsolutePath();;
    public String tempPath = Files.createTempDirectory("flauncherprobe").getParent().toRealPath().toFile().getAbsolutePath();;
    public String lastServer = "";
    public boolean noLogRotation = false;
    public boolean autoConnect = false;

    private final Path configFile;

    public FactorioLauncherConfig() throws IOException {
        this(OSHelper.getConfigPath());
    }

    public FactorioLauncherConfig(Path configFile) throws IOException {
        this.configFile = configFile;
        load();
    }

    public void load() throws IOException {
        JsonObject object = Json.parse(new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8)).asObject();
        factorioPath = object.getString("factoriopath", factorioPath);
        modCachePath = object.getString("modcachepath", modCachePath);
        tempPath = object.getString("temppath", tempPath);
        lastServer = object.getString("lastserver", lastServer);
        noLogRotation = object.getBoolean("nologrotation", noLogRotation);
        autoConnect = object.getBoolean("autoconnect", autoConnect);
    }

    public void save() throws IOException {
        JsonObject object = new JsonObject();
        object.add("factoriopath", factorioPath);
        object.add("modcachepath", modCachePath);
        object.add("temppath", tempPath);
        object.add("lastserver", lastServer);
        object.add("nologrotation", noLogRotation);
        object.add("autoconnect", autoConnect);
        Files.write(configFile, object.toString().getBytes(StandardCharsets.UTF_8));
    }
}

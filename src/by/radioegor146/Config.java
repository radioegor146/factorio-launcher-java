/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146;

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
public final class Config {

    public String factorioPath = "";
    public String lastServer = "";
    public boolean noLogRotation = false;
    public boolean autoConnect = false;

    private final Path configFile;

    public Config() {
        this.configFile = Paths.get(System.getProperty("user.home"), "flauncher.config");
    }

    public Config(Path configFile) throws IOException {
        this.configFile = configFile;
        load();
    }

    public void load() throws IOException {
        JsonObject object = Json.parse(new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8)).asObject();
        factorioPath = object.getString("factoriopath", "Не задано");
        lastServer = object.getString("lastserver", "");
        noLogRotation = object.getBoolean("nologrotation", false);
        autoConnect = object.getBoolean("autoconnect", false);
    }

    public void save() throws IOException {
        JsonObject object = new JsonObject();
        object.add("factoriopath", factorioPath);
        object.add("lastserver", lastServer);
        object.add("nologrotation", noLogRotation);
        object.add("autoconnect", autoConnect);
        Files.write(configFile, object.toString().getBytes(StandardCharsets.UTF_8));
    }
}

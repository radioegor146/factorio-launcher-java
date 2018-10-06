/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.LinkOption;
import java.io.IOException;
/**
 *
 * @author radioegor146
 */
public class ModsHelper {
    public void prepare() throws IOException {
        if (Files.notExists(Paths.get(FactorioLauncher.config.factorioPath, "launcher")))
            Files.createDirectories(Paths.get(FactorioLauncher.config.factorioPath, "launcher"));
        if (Files.notExists(Paths.get(FactorioLauncher.config.factorioPath, "launcher", "current")))
            Files.createDirectories(Paths.get(FactorioLauncher.config.factorioPath, "launcher", "current"));
        if (Files.notExists(Paths.get(FactorioLauncher.config.factorioPath, "launcher", "modcache")))
            Files.createDirectories(Paths.get(FactorioLauncher.config.factorioPath, "launcher", "modcache"));
    }
}

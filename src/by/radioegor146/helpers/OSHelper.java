/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146.helpers;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author radioegor146
 */
public class OSHelper {

    public static boolean isWindows() {
        return System.getProperty("os.name").contains("Windows");
    }

    public static Path getDefaultModCacheDir() {
        if (isWindows()) {
            return Paths.get(System.getenv("LOCALAPPDATA"), "FactorioLauncher");
        } else {
            if (System.getenv().get("XDG_CONFIG_HOME") != null) {
                return Paths.get(System.getenv().get("XDG_CONFIG_HOME"), "FactorioLauncher");
            }
            return Paths.get(System.getProperty("user.home"), ".config", "FactorioLauncher");
        }
    }

    public static Path getConfigPath() {
        if (isWindows()) {
            return Paths.get(System.getenv("LOCALAPPDATA"), "FactorioLauncher", "main.config");
        } else {
            if (System.getenv().get("XDG_CONFIG_HOME") != null) {
                return Paths.get(System.getenv().get("XDG_CONFIG_HOME"), "FactorioLauncher", "main.config");
            }
            return Paths.get(System.getProperty("user.home"), ".config", "FactorioLauncher", "main.config");
        }
    }
}

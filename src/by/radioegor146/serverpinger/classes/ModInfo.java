/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146.serverpinger.classes;

import by.radioegor146.serverpinger.utils.DataStreamUtils;
import by.radioegor146.serverpinger.utils.LittleEndianInputStream;
import by.radioegor146.serverpinger.utils.LittleEndianOutputStream;
import java.io.IOException;

/**
 *
 * @author radioegor146
 */
public class ModInfo {

    public String name;
    public Version version;
    public int crc;

    public ModInfo(String name, Version version, int crc) {
        this.name = name;
        this.version = version;
        this.crc = crc;
    }

    public ModInfo() {
    }

    public ModInfo read(LittleEndianInputStream stream) throws IOException {
        name = DataStreamUtils.readString(stream);
        version = new Version().read(stream);
        crc = stream.readInt();
        return this;
    }

    public void write(LittleEndianOutputStream stream) throws IOException {
        DataStreamUtils.writeString(stream, name);
        version.write(stream);
        stream.writeInt(crc);
    }

    @Override
    public String toString() {
        return name + " " + version;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        ModInfo e = (ModInfo) o;
        return e.toString().equals(toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}

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
public class Version {

    public short majorVersion;
    public short minorVersion;
    public short subVersion;

    public Version(short majorVersion, short minorVersion, short subVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.subVersion = subVersion;
    }

    public Version() {
    }

    public Version read(LittleEndianInputStream stream) throws IOException {
        majorVersion = DataStreamUtils.readVarShort(stream);
        minorVersion = DataStreamUtils.readVarShort(stream);
        subVersion = DataStreamUtils.readVarShort(stream);
        return this;
    }

    public void write(LittleEndianOutputStream stream) throws IOException {
        DataStreamUtils.writeVarShort(stream, this.majorVersion);
        DataStreamUtils.writeVarShort(stream, this.minorVersion);
        DataStreamUtils.writeVarShort(stream, this.subVersion);
    }

    @Override
    public String toString() {
        return majorVersion + "." + minorVersion + "." + subVersion;
    }
}

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
public class ModSettings {

    public byte type;
    public String name;
    public Object value;

    public ModSettings(byte type, short value) {
        this.type = type;
        this.value = value;
    }

    public ModSettings() {
    }

    public ModSettings read(LittleEndianInputStream stream) throws IOException {
        type = stream.readByte();
        switch (type) {
            case 1:
            case 2:
            case 3:
            case 4:
                name = DataStreamUtils.readComplexString(stream);
                break;
            default:
                throw new IOException("Wrong setting type");
        }
        switch (type) {
            case 1:
                value = stream.readBoolean();
                break;
            case 2:
                value = stream.readDouble();
                break;
            case 3:
                value = stream.readLong();
                break;
            case 4:
                value = DataStreamUtils.readComplexString(stream);
                break;
            default:
                throw new IOException("Wrong setting type");
        }
        return this;
    }

    public void write(LittleEndianOutputStream stream) throws IOException {

    }

    @Override
    public String toString() {
        return name + "=" + value;
    }
}

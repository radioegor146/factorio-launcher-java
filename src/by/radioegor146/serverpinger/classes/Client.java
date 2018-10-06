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
public class Client {

    public short peerId;
    public String username;
    public byte droppingProgress = -1;
    public byte mapSavingProgress = -1;
    public byte mapDownloadingProgress = -1;
    public byte mapLoadingProgress = -1;
    public byte tryingToCatchUpProgress = -1;

    public Client read(LittleEndianInputStream stream) throws IOException {
        peerId = DataStreamUtils.readVarShort(stream);
        username = DataStreamUtils.readString(stream);
        byte flags = stream.readByte();
        if ((flags & 0x01) > 0) {
            droppingProgress = stream.readByte();
        }
        if ((flags & 0x02) > 0) {
            mapSavingProgress = stream.readByte();
        }
        if ((flags & 0x04) > 0) {
            mapDownloadingProgress = stream.readByte();
        }
        if ((flags & 0x08) > 0) {
            mapLoadingProgress = stream.readByte();
        }
        if ((flags & 0x10) > 0) {
            tryingToCatchUpProgress = stream.readByte();
        }
        return this;
    }

    public void write(LittleEndianOutputStream stream) throws IOException {

    }

    @Override
    public String toString() {
        return "peerId: " + peerId + "; username: " + username;
    }
}

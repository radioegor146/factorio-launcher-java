/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146.serverpinger.messages;

import by.radioegor146.serverpinger.ClientMessage;
import by.radioegor146.serverpinger.classes.Version;
import by.radioegor146.serverpinger.utils.LittleEndianOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author radioegor146
 */
public class ConnectionRequestMessage extends ClientMessage {

    public Version version = new Version();
    public short buildVersion;
    public int connectionRequestIDGeneratedOnClient;

    @Override
    public byte[] getBytes() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            LittleEndianOutputStream stream = new LittleEndianOutputStream(bos);
            version.write(stream);
            stream.writeShort(buildVersion);
            stream.writeInt(connectionRequestIDGeneratedOnClient);
            return bos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    @Override
    public byte getMessageType() {
        return 2;
    }
}

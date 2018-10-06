/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146.serverpinger.messages;

import by.radioegor146.serverpinger.ServerMessage;
import by.radioegor146.serverpinger.classes.Version;
import by.radioegor146.serverpinger.utils.LittleEndianInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *
 * @author radioegor146
 */
public class ConnectionRequestReplyMessage extends ServerMessage {

    public Version version;
    public short buildVersion;
    public int connectionRequestIDGeneratedOnClient;
    public int connectionRequestIDGeneratedOnServer;

    @Override
    public void fromBytes(byte[] data) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        LittleEndianInputStream stream = new LittleEndianInputStream(bis);
        version = new Version().read(stream);
        buildVersion = (short) stream.readShort();
        connectionRequestIDGeneratedOnClient = stream.readInt();
        connectionRequestIDGeneratedOnServer = stream.readInt();
    }

    @Override
    public byte getMessageType() {
        return 3;
    }

}

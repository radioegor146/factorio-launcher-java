/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146.serverpinger.messages;

import by.radioegor146.serverpinger.ClientMessage;
import by.radioegor146.serverpinger.classes.ModInfo;
import by.radioegor146.serverpinger.classes.ModSettings;
import by.radioegor146.serverpinger.utils.DataStreamUtils;
import by.radioegor146.serverpinger.utils.LittleEndianOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author radioegor146
 */
public class ConnectionRequestReplyConfirmMessage extends ClientMessage {

    public int connectionRequestIDGeneratedOnClient;
    public int connectionRequestIDGeneratedOnServer;
    public int instanceID;
    public String username = "factoriolauncher";
    public String passwordHash = "";
    public String serverKey = "";
    public String serverKeyTimestamp = "";
    public int coreChecksum;
    public ModInfo[] mods = new ModInfo[0];
    public ModSettings[] modSettings = new ModSettings[0];

    @Override
    public byte[] getBytes() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            LittleEndianOutputStream stream = new LittleEndianOutputStream(bos);
            stream.writeInt(connectionRequestIDGeneratedOnClient);
            stream.writeInt(connectionRequestIDGeneratedOnServer);
            stream.writeInt(instanceID);
            DataStreamUtils.writeString(stream, username);
            DataStreamUtils.writeString(stream, passwordHash);
            DataStreamUtils.writeString(stream, serverKey);
            DataStreamUtils.writeString(stream, serverKeyTimestamp);
            stream.writeInt(coreChecksum);
            DataStreamUtils.writeVarInt(stream, mods.length);
            for (ModInfo mod : mods) {
                mod.write(stream);
            }
            DataStreamUtils.writeVarInt(stream, modSettings.length);
            for (ModSettings mod : modSettings) {
                mod.write(stream);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    @Override
    public byte getMessageType() {
        return 4;
    }
}

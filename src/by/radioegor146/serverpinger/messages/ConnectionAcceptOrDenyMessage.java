/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146.serverpinger.messages;

import by.radioegor146.serverpinger.ServerMessage;
import by.radioegor146.serverpinger.classes.AddressToUsernameMapping;
import by.radioegor146.serverpinger.classes.Client;
import by.radioegor146.serverpinger.classes.ListItem;
import by.radioegor146.serverpinger.classes.ModInfo;
import by.radioegor146.serverpinger.classes.ModSettings;
import by.radioegor146.serverpinger.classes.Version;
import by.radioegor146.serverpinger.utils.DataStreamUtils;
import by.radioegor146.serverpinger.utils.LittleEndianInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;

/**
 *
 * @author radioegor146
 */
public class ConnectionAcceptOrDenyMessage extends ServerMessage {

    public boolean online;

    public int clientRequestId;
    public byte status;
    public String gameName;
    public String serverHash;
    public String description;
    public byte latency;
    public int gameId;

    public String serverUsername;
    public byte mapSavingProgress;
    public short var0;
    public Client[] clients;

    public int firstSequenceNumberToExpect;
    public int firstSequenceNumberToSend;
    public short newPeerId;

    public ModInfo[] mods;

    public ModSettings[] modSettings;

    public short pausedBy;

    public int lanGameId;
    public String name;
    public Version applicationVersion;
    public short buildVersion;
    public String serverDescription;
    public short maxPlayers;
    public int gameTimeElapsed;
    public boolean hasPassword;
    public String hostAddress;

    public String[] tags;

    public String serverUsername1;
    public int autosaveInterval;
    public int autosaveSlots;
    public int AFKAutoKickInterval;
    public boolean allowCommands;
    public int maxUploadInKilobytesPerSecond;
    public byte minimumLatencyInTicks;
    public boolean ignorePlayerLimitForReturningPlayers;
    public boolean onlyAdminsCanPauseTheGame;
    public boolean requireUserVerification;

    public HashSet<String> admins;

    public ListItem[] whitelist;
    public AddressToUsernameMapping[] whitelistMappings;

    public ListItem[] banlist;
    public AddressToUsernameMapping[] banlistMappings;

    @Override
    public void fromBytes(byte[] data) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        LittleEndianInputStream stream = new LittleEndianInputStream(bis);
        clientRequestId = stream.readInt();
        status = stream.readByte();
        gameName = DataStreamUtils.readString(stream);
        serverHash = DataStreamUtils.readString(stream);
        description = DataStreamUtils.readString(stream);
        latency = stream.readByte();
        gameId = stream.readInt();
        serverUsername = DataStreamUtils.readString(stream);
        mapSavingProgress = stream.readByte();
        var0 = DataStreamUtils.readVarShort(stream);
        short clientsCount = DataStreamUtils.readVarShort(stream);
        clients = new Client[clientsCount];
        for (int i = 0; i < clientsCount; i++) {
            clients[i] = new Client().read(stream);
        }
        firstSequenceNumberToExpect = stream.readInt();
        firstSequenceNumberToSend = stream.readInt();
        newPeerId = stream.readShort();
        short modsCount = DataStreamUtils.readVarShort(stream);
        mods = new ModInfo[modsCount];
        for (int i = 0; i < modsCount; i++) {
            mods[i] = new ModInfo().read(stream);
        }
        short modSettingsCount = DataStreamUtils.readVarShort(stream);
        modSettings = new ModSettings[modSettingsCount];
        for (int i = 0; i < modSettingsCount; i++) {
            modSettings[i] = new ModSettings().read(stream);
        }
        pausedBy = stream.readShort();
        lanGameId = stream.readInt();
        name = DataStreamUtils.readString(stream);
        applicationVersion = new Version().read(stream);
        buildVersion = stream.readShort();
        serverDescription = DataStreamUtils.readString(stream);
        maxPlayers = stream.readShort();
        gameTimeElapsed = stream.readInt();
        hasPassword = stream.readBoolean();
        int hostAddressStringLength = stream.readInt();
        byte[] hostAddressStringBytes = new byte[hostAddressStringLength];
        stream.read(hostAddressStringBytes);
        hostAddress = new String(hostAddressStringBytes, Charset.forName("UTF-8"));
        int tagsCount = DataStreamUtils.readVarInt(stream);
        tags = new String[tagsCount];
        for (int i = 0; i < tagsCount; i++) {
            tags[i] = DataStreamUtils.readString(stream);
        }
        serverUsername1 = DataStreamUtils.readString(stream);
        autosaveInterval = stream.readInt();
        autosaveSlots = stream.readInt();
        AFKAutoKickInterval = stream.readInt();
        allowCommands = stream.readBoolean();
        maxUploadInKilobytesPerSecond = stream.readInt();
        minimumLatencyInTicks = stream.readByte();
        ignorePlayerLimitForReturningPlayers = stream.readBoolean();
        onlyAdminsCanPauseTheGame = stream.readBoolean();
        requireUserVerification = stream.readBoolean();
        int adminsCount = DataStreamUtils.readVarInt(stream);
        admins = new HashSet();
        for (int i = 0; i < adminsCount; i++) {
            admins.add(DataStreamUtils.readString(stream));
        }
        int whitelistCount = DataStreamUtils.readVarInt(stream);
        whitelist = new ListItem[whitelistCount];
        for (int i = 0; i < whitelistCount; i++) {
            whitelist[i] = new ListItem().read(stream);
        }
        int whitelistMappingsCount = DataStreamUtils.readVarInt(stream);
        whitelistMappings = new AddressToUsernameMapping[whitelistMappingsCount];
        for (int i = 0; i < whitelistMappingsCount; i++) {
            whitelistMappings[i] = new AddressToUsernameMapping().read(stream);
        }
        int banlistCount = DataStreamUtils.readVarInt(stream);
        banlist = new ListItem[banlistCount];
        for (int i = 0; i < banlistCount; i++) {
            banlist[i] = new ListItem().read(stream);
        }
        int banlistMappingsCount = DataStreamUtils.readVarInt(stream);
        banlistMappings = new AddressToUsernameMapping[banlistMappingsCount];
        for (int i = 0; i < banlistMappingsCount; i++) {
            banlistMappings[i] = new AddressToUsernameMapping().read(stream);
        }
        online = true;
    }

    @Override
    public byte getMessageType() {
        return 5;
    }

}

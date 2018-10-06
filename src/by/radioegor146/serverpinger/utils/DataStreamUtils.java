/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146.serverpinger.utils;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 *
 * @author radioegor146
 */
public class DataStreamUtils {

    public static short readVarShort(LittleEndianInputStream stream) throws IOException {
        int ansValue;
        if ((ansValue = (int) (stream.readByte() & 0xFF)) == 255) {
            ansValue = stream.readShort();
        }
        return (short) ansValue;
    }

    public static void writeVarShort(LittleEndianOutputStream stream, short data) throws IOException {
        if (data > 0xFF) {
            stream.writeByte(0xFF);
            stream.writeShort(data);
        } else {
            stream.writeByte(data);
        }
    }

    public static int readVarInt(LittleEndianInputStream stream) throws IOException {
        int ansValue;
        if ((ansValue = (int) (stream.readByte() & 0xFF)) == 255) {
            ansValue = stream.readInt();
        }
        return ansValue;
    }

    public static void writeVarInt(LittleEndianOutputStream stream, int data) throws IOException {
        if (data > 0xFF) {
            stream.writeByte(0xFF);
            stream.writeInt(data);
        } else {
            stream.writeByte(data);
        }
    }

    public static String readString(LittleEndianInputStream stream) throws IOException {
        short stringBytesLength = readVarShort(stream);
        byte[] data = new byte[stringBytesLength];
        stream.read(data);
        return new String(data, Charset.forName("UTF-8"));
    }

    public static void writeString(LittleEndianOutputStream stream, String data) throws IOException {
        byte[] stringBytes = data.getBytes(Charset.forName("UTF-8"));
        writeVarShort(stream, (short) stringBytes.length);
        stream.write(stringBytes);
    }

    public static String readComplexString(LittleEndianInputStream stream) throws IOException {
        int stringBytesLength = readVarInt(stream);
        byte[] data = new byte[stringBytesLength];
        stream.read(data);
        return new String(data, Charset.forName("UTF-8"));
    }

    public static void writeComplexString(LittleEndianOutputStream stream, String data) throws IOException {
        byte[] stringBytes = data.getBytes(Charset.forName("UTF-8"));
        writeVarInt(stream, stringBytes.length);
        stream.write(stringBytes);
    }
}

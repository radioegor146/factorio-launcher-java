/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146.serverpinger;

import by.radioegor146.serverpinger.utils.DataStreamUtils;
import by.radioegor146.serverpinger.utils.LittleEndianInputStream;
import by.radioegor146.serverpinger.utils.LittleEndianOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

/**
 *
 * @author radioegor146
 */
public class FactorioNetMessage {

    public byte[] packetBytes;

    public byte type;
    public short messageId;
    public boolean isFragmented;
    public short fragmentId;

    public boolean isLastFragment;

    public short currentMessageId;
    public short lastServerMessageId;

    public FactorioNetMessage(DatagramPacket packet) throws Exception {
        LittleEndianInputStream dis = new LittleEndianInputStream(new ByteArrayInputStream(packet.getData()));
        int offset = 0;
        type = dis.readByte();
        offset++;
        type = (byte) (type & 0b11011111);
        isFragmented = (type & 0b01000000) > 0;
        isLastFragment = (type & 0b10000000) > 0;
        type = (byte) (type & 0b00011111);
        if ((type >= 2 && type <= 5) || isFragmented || isLastFragment) {
            messageId = dis.readShort();
            offset += 2;
            if (isFragmented) {
                if ((fragmentId = dis.readByte()) == 0xFF) {
                    fragmentId = dis.readShort();
                    offset += 2;
                }
                offset++;
            }
        }
        if ((messageId & (int) 0b1000000000000000) > 0) {
            dis.readByte();
            offset++;
            dis.readInt();
            offset += 4;
        }
        messageId = (short) (messageId & 0b0111111111111111);
        packetBytes = new byte[packet.getLength() - offset];
        dis.read(packetBytes);
    }

    public FactorioNetMessage() {
    }

    public DatagramPacket getPacket() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            LittleEndianOutputStream dos = new LittleEndianOutputStream(bos);
            boolean isReliable = (type >= 2 && type <= 5);
            if (isFragmented) {
                type |= 0b01000000;
            }
            if (isLastFragment) {
                type |= 0b10000000;
            }
            dos.writeByte(type);
            if (isReliable || isFragmented) {
                messageId = currentMessageId;
                dos.writeShort(type | (lastServerMessageId > 0 ? 0x8000 : 0));
                if (isFragmented) {
                    DataStreamUtils.writeVarShort(dos, fragmentId);
                }
            }
            if (lastServerMessageId > 0) {
                dos.writeByte(1);
                dos.writeInt(lastServerMessageId);
            }
            dos.write(packetBytes);
            return new DatagramPacket(bos.toByteArray(), bos.size());
        } catch (IOException e) {
            return null;
        }
    }

    public FactorioNetMessage(byte type, byte[] data) {
        this.packetBytes = data;
        this.type = type;
    }
}

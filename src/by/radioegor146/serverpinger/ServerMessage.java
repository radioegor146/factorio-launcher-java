/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146.serverpinger;

import java.io.IOException;

/**
 *
 * @author radioegor146
 */
public abstract class ServerMessage {

    public abstract void fromBytes(byte[] data) throws IOException;

    public abstract byte getMessageType();
}

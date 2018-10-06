/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146.serverpinger;

/**
 *
 * @author radioegor146
 */
public abstract class ClientMessage {

    public abstract byte[] getBytes();

    public abstract byte getMessageType();
}
